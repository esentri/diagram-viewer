package io.domainlifecycles.diagramviewer.rest.kroki;


import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KrokiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(KrokiClient.class);
    private static final String KROKI_NOMNOML_SVG_PATH = "/nomnoml/svg";

    private final String krokiContainerUrl;

    public KrokiClient(@Value("${kroki.container.url}") String krokiContainerUrl) {
        this.krokiContainerUrl = krokiContainerUrl;
    }

    public byte[] convertTo(final String rawNomNomlContent, final FileType fileType) {
        final String path = getKrokiPath(fileType);
        return convert(rawNomNomlContent, path);
    }

    private byte[] convert(String rawInputDiagramContent, String path) {
        LOGGER.debug("Converting Nomnoml diagram to specified format via Kroki Docker container...");

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(krokiContainerUrl + path))
            .header("Content-Type", "text/plain")
            .timeout(Duration.ofSeconds(10))
            .POST(BodyPublishers.ofString(rawInputDiagramContent))
            .build();

        return send(request);
    }

    private byte[] send(final HttpRequest httpRequest) {
        try {
            LOGGER.debug(String.format("Sending HTTP request '%s' to Kroki Docker container.",
                httpRequest.bodyPublisher().orElseGet(() -> BodyPublishers.ofString("Request body empty!"))));
            final HttpResponse<byte[]> response = HttpClient
                .newHttpClient()
                .send(httpRequest, BodyHandlers.ofByteArray());

            if (response.statusCode() < 400) {
                LOGGER.debug("HTTP request to Kroki Docker container has been successful.");
                return response.body();
            }
            if (response.statusCode() >= 400) {
                throw DiagramViewerException.fail(
                    String.format("Kroki Docker container returned error for conversion: %s",
                        new String(response.body(), StandardCharsets.UTF_8)));
            }
        } catch (IOException | InterruptedException e) {
            throw DiagramViewerException.fail("Nomnoml conversion with Kroki Server failed.", e);
        }
        throw DiagramViewerException.fail("Kroki server couldn't be reached.");
    }

    private String getKrokiPath(final FileType fileType) {
        String krokiPath;
        switch (fileType) {
            case SVG -> krokiPath = KROKI_NOMNOML_SVG_PATH;
            default -> throw DiagramViewerException.fail(
                String.format("Filetype '%s' not allowed for Kroki conversion.", fileType));
        }
        return krokiPath;
    }
}
