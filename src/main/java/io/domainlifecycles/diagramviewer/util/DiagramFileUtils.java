package io.domainlifecycles.diagramviewer.util;

import io.domainlifecycles.diagramviewer.rest.ResourceController;
import java.time.LocalDateTime;

public class DiagramFileUtils {

    public static String assembleDiagramUrl(String... diagramSrc) {
        return assembleRequestUrl(diagramSrc) + getDummyRequestParameter();
    }

    private static String assembleRequestUrl(String... diagramSrc) {
        return ResourceController.RESOURCES_API_PATH + "/" + String.join("/", diagramSrc);
    }

    private static String getDummyRequestParameter() {
        return "?" + ResourceController.TIMESTAMP_REQUEST_PARAMETER_NAME + "=" + LocalDateTime.now();
    }
}
