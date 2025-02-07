package io.domainlifecycles.diagramviewer.kroki;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KrokiClient {

    @Value("${kroki.url}")
    private String krokiUrl;
    private static final Logger log = LoggerFactory.getLogger(KrokiClient.class);

    private static final String KROKI_NOMNOML_SVG_PATH = "/nomnoml/svg";

    private static final Integer MAX_RETRIES = 5;
    private static final Integer WAIT_TIMEOUT_MS = 500;

    public byte[] convertNomnomlToSVG(String rawInputDiagramContent) {
        log.info("Converting Nomnoml diagram to specified format via Kroki.");

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(krokiUrl + KROKI_NOMNOML_SVG_PATH))
            .header("Accept", "text/plain")
            .POST(BodyPublishers.ofString(rawInputDiagramContent))
            .build();

        return sendWithRetries(request);
    }

    private byte[] sendWithRetries(final HttpRequest httpRequest) {
        for(int retryCounter = 0; retryCounter < MAX_RETRIES; retryCounter++) {
            try {
                log.debug(String.format("Sending HTTP request to Kroki. Retry: %s", retryCounter + 1));
                final HttpResponse<byte[]> response = HttpClient.newHttpClient().send(httpRequest, BodyHandlers.ofByteArray());

                if (response.statusCode() < 400) {
                    log.debug("HTTP request to Kroki has been successful.");
                    return response.body();
                }
            } catch (IOException | InterruptedException e) {
                try {
                    TimeUnit.MILLISECONDS.sleep(WAIT_TIMEOUT_MS);
                } catch (InterruptedException ignored) { }
            }
        }
        throw DiagramViewerException.fail(String.format("Kroki server couldn't be reached in specified retry limit (Retries: %s, Timeout: %s)", MAX_RETRIES, WAIT_TIMEOUT_MS));
    }

}
