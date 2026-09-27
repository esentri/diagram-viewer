/*
 *     ________  .__
 *     \______ \ |__|____     ________________    _____
 *      |    |  \|  \__  \   / ___\_  __ \__  \  /     \
 *      |    |   \  |/ __ \_/ /_/  >  | \// __ \|  Y Y  \
 *     /_______  /__(____  /\___  /|__|  (____  /__|_|  /
 *             \/        \//_____/            \/      \/
 *     ____   ____.__
 *     \   \ /   /|__| ______  _  __ ___________
 *      \   Y   / |  |/ __ \ \/ \/ // __ \_  __ \
 *       \     /  |  \  ___/\     /\  ___/|  | \/
 *        \___/   |__|\___  >\/\_/  \___  >__|
 *                        \/            \/
 *
 *  Copyright 2025-2026 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.domainlifecycles.diagramviewer.rest.kroki;


import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Converts nomnoml diagrams to SVG via the Kroki container.
 * <p>
 * One {@link HttpClient} is shared by all conversions instead of creating one per call. The request timeout is
 * configurable: large diagrams take Kroki well over ten seconds (the whole
 * esprit_2 model: 4529 classes, 17 s, 7 MB SVG), and Kroki itself aborts conversions after
 * {@code KROKI_COMMAND_TIMEOUT} (default 5 s), which it reports as error 500.
 */
@Service
public class KrokiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(KrokiClient.class);
    private static final String KROKI_NOMNOML_SVG_PATH = "/nomnoml/svg";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);

    private final URI nomnomlSvgUri;
    private final Duration requestTimeout;
    private final HttpClient httpClient;

    public KrokiClient(
        @Value("${kroki.container.url}") String krokiContainerUrl,
        @Value("${kroki.request.timeoutSeconds:90}") long requestTimeoutSeconds
    ) {
        this.nomnomlSvgUri = URI.create(krokiContainerUrl + KROKI_NOMNOML_SVG_PATH);
        this.requestTimeout = Duration.ofSeconds(requestTimeoutSeconds);
        this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(CONNECT_TIMEOUT)
            .build();
    }

    public byte[] convert(String rawInputDiagramContent) {
        LOGGER.debug("Converting Nomnoml diagram of {} characters to SVG via Kroki Docker container...",
            rawInputDiagramContent.length());

        HttpRequest request = HttpRequest.newBuilder()
            .uri(nomnomlSvgUri)
            .header("Content-Type", "text/plain")
            .timeout(requestTimeout)
            .POST(BodyPublishers.ofString(rawInputDiagramContent))
            .build();

        return send(request);
    }

    private byte[] send(final HttpRequest httpRequest) {
        try {
            final HttpResponse<byte[]> response = httpClient.send(httpRequest, BodyHandlers.ofByteArray());

            if (response.statusCode() < 400) {
                LOGGER.debug("HTTP request to Kroki Docker container has been successful.");
                return response.body();
            }
            throw DiagramViewerException.fail(
                String.format("Kroki Docker container returned error for conversion: %s",
                    new String(response.body(), StandardCharsets.UTF_8)));
        } catch (HttpTimeoutException e) {
            throw DiagramViewerException.fail(String.format(
                "Nomnoml conversion with Kroki Server did not finish within %d seconds. The diagram is probably too large - restrict it with filters.",
                requestTimeout.toSeconds()), e);
        } catch (IOException e) {
            throw DiagramViewerException.fail("Nomnoml conversion with Kroki Server failed.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw DiagramViewerException.fail("Nomnoml conversion with Kroki Server was interrupted.", e);
        }
    }
}
