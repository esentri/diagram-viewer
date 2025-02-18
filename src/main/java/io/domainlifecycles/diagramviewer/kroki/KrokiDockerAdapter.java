package io.domainlifecycles.diagramviewer.kroki;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.github.dockerjava.api.model.HostConfig.newHostConfig;

class KrokiDockerAdapter {

    private final static String KROKI_CONTAINER_NAME = "kroki";
    private final static String KROKI_CONTAINER_HOST_NAME = "esentri";
    private final static String KROKI_CONTAINER_IMAGE_NAME = "yuzutech/kroki";
    private final static Logger log = LoggerFactory.getLogger(KrokiDockerAdapter.class);

    private final DockerClient dockerClient;
    private final String krokiContainerId;

    KrokiDockerAdapter() {
        dockerClient = initializeDockerClient();
        krokiContainerId = createOrGetKrokiDockerContainerId();
        start(krokiContainerId);
    }

    void stop() {
        try {
            dockerClient.stopContainerCmd(krokiContainerId).exec();
        } catch(RuntimeException e) {
            throw DiagramViewerException.fail("Could not stop Kroki Docker container.", e);
        }
        log.debug("Kroki container stopped");
    }

    private void start(final String containerId) {
        try {
            dockerClient.startContainerCmd(containerId).exec();
        } catch(RuntimeException e) {
            throw DiagramViewerException.fail(
                "Could not start Kroki Docker container. Please check whether you have another instance of this container running.", e);
        }
        log.debug("Kroki container started");
    }

    private String createOrGetKrokiDockerContainerId() {
        final List<Container> foundKrokiContainers = dockerClient
            .listContainersCmd()
            .withAncestorFilter(List.of(KROKI_CONTAINER_IMAGE_NAME))
            .exec();

        if(!foundKrokiContainers.isEmpty()) {
            String krokiContainerId = foundKrokiContainers.get(0).getId();
            log.debug(String.format("Found existing Kroki Docker container with ID: %s. Reusing this.", krokiContainerId));
            return krokiContainerId;
        }

        log.debug("No existing Kroki Docker container found. Creating new one...");
        return createKrokiDockerContainer();
    }

    private String createKrokiDockerContainer() {
        final ExposedPort tcp4444 = ExposedPort.tcp(8000);
        final Ports portBindings = new Ports();
        portBindings.bind(tcp4444, Ports.Binding.bindPort(8000));

        try {
            return dockerClient.createContainerCmd(KROKI_CONTAINER_IMAGE_NAME)
                .withName(KROKI_CONTAINER_NAME)
                .withHostName(KROKI_CONTAINER_HOST_NAME)
                .withExposedPorts(tcp4444)
                .withHostConfig(newHostConfig().withPortBindings(portBindings))
                .exec()
                .getId();
        } catch(RuntimeException e) {
            throw DiagramViewerException.fail(
                "Could not create Kroki Docker container. Please check whether your Docker engine is up and running.", e);
        }
    }

    private DockerClient initializeDockerClient() {
        final DockerClientConfig standard = DefaultDockerClientConfig
            .createDefaultConfigBuilder()
            .withDockerTlsVerify(false)
            .build();

        final DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
            .dockerHost(standard.getDockerHost())
            .maxConnections(100)
            .connectionTimeout(Duration.ofSeconds(30))
            .responseTimeout(Duration.ofSeconds(45))
            .build();

        try {
            return DockerClientImpl.getInstance(standard, httpClient);
        } catch(RuntimeException e) {
            throw DiagramViewerException.fail(
                "Could not establish connection to Docker client. Please check whether your Docker engine is up and running.", e);
        }
    }
}
