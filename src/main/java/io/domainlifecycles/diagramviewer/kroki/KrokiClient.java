package io.domainlifecycles.diagramviewer.kroki;


import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class KrokiClient {

    private static final Logger log = LoggerFactory.getLogger(KrokiDockerAdapter.class);

    private static final String KROKI_CONTAINER_URL = "http://localhost:8000";
    private static final String KROKI_NOMNOML_SVG_PATH = "/nomnoml/svg";

    private static final Integer MAX_RETRIES = 5;
    private static final Integer WAIT_TIMEOUT_MS = 500;

    private final KrokiDockerAdapter krokiDockerAdapter;

    public KrokiClient() {
        this.krokiDockerAdapter = new KrokiDockerAdapter();
    }

    /**
     * Has to be called after all Kroki actions have been performed, otherwise Docker-Container
     * keeps running.
     */
    public void finish() {
        krokiDockerAdapter.stop();
    }

    public byte[] convertTo(final String rawNomNomlContent, final FileType fileType) {
        final String path = getKrokiPath(fileType);
        return convert(rawNomNomlContent, path);
    }

    private byte[] convert(String rawInputDiagramContent, String path) {
        log.info("Converting Nomnoml diagram to specified format via Kroki Docker container.");

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(KROKI_CONTAINER_URL + path))
            .header("Accept", "text/plain")
            .POST(BodyPublishers.ofString(rawInputDiagramContent))
            .build();

        return sendWithRetries(request);
    }

    private byte[] sendWithRetries(final HttpRequest httpRequest) {

        for(int retryCounter = 0; retryCounter < MAX_RETRIES; retryCounter++) {
            try {
                log.debug(String.format("Sending HTTP request to Kroki Docker container. Retry: %s", retryCounter + 1));
                final HttpResponse<byte[]> response = HttpClient.newHttpClient().send(httpRequest, BodyHandlers.ofByteArray());

                if (response.statusCode() < 400) {
                    log.debug("HTTP request to Kroki Docker container has been successful.");
                    return response.body();
                }
                if (response.statusCode() >= 400) {
                    krokiDockerAdapter.stop();
                    throw DiagramViewerException.fail(String.format("Kroki Docker container returned error for conversion: %s",
                        new String(response.body(), StandardCharsets.UTF_8)));
                }
            } catch (IOException | InterruptedException e) {
                try {
                    TimeUnit.MILLISECONDS.sleep(WAIT_TIMEOUT_MS);
                } catch (InterruptedException ignored) { }
            }
        }
        krokiDockerAdapter.stop();
        throw DiagramViewerException.fail(String.format("Kroki server couldn't be reached in specified retry limit (Retries: %s, Timeout: %s)", MAX_RETRIES, WAIT_TIMEOUT_MS));
    }

    private String getKrokiPath(final FileType fileType) {
        String krokiPath;
        switch (fileType) {
            case SVG -> krokiPath = KROKI_NOMNOML_SVG_PATH;
            default -> throw DiagramViewerException.fail(String.format("Filetype %s not allowed for Kroki conversion", fileType));
        }
        return krokiPath;
    }
}
