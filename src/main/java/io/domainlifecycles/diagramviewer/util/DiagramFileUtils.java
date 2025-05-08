package io.domainlifecycles.diagramviewer.util;

import io.domainlifecycles.diagramviewer.rest.api.ResourceController;
import java.time.Instant;

public class DiagramFileUtils {

    public static String assembleDiagramUrl(Instant diagramLastModified, Instant stylingLastModified, String... diagramSrc) {
        return assembleRequestUrl(diagramSrc) + getLastModifiedRequestParameters(diagramLastModified, stylingLastModified);
    }

    private static String assembleRequestUrl(String... diagramSrc) {
        return ResourceController.RESOURCES_API_PATH + "/" + String.join("/", diagramSrc);
    }

    private static String getLastModifiedRequestParameters(Instant diagramLastModified, Instant stylingLastModified) {
        return "?" + ResourceController.DIAGRAM_LAST_MODIFIED_REQUEST_PARAMETER_NAME + "=" + diagramLastModified
            + "&" + ResourceController.STYLING_LAST_MODIFIED_REQUEST_PARAMETER_NAME + "=" + stylingLastModified;
    }
}
