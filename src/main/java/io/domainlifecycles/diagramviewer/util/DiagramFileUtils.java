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

package io.domainlifecycles.diagramviewer.util;

import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.rest.api.ResourceController;
import java.nio.file.Path;
import java.time.Instant;

public class DiagramFileUtils {

    public static final String SVG_FILE_SUFFIX = ".svg";

    /**
     * @param diagram a diagram
     * @return the file name of the diagram's image: named after the diagram's id, since names are only unique within
     * a directory of a project
     */
    public static String imageFileName(Diagram diagram) {
        return diagram.getId() + SVG_FILE_SUFFIX;
    }

    /**
     * @param diagramsLocation the directory holding the images of all projects
     * @param diagram          a diagram
     * @return where the diagram's image is stored
     */
    public static Path imagePath(String diagramsLocation, Diagram diagram) {
        return Path.of(diagramsLocation, diagram.getProject().getId().toString(), imageFileName(diagram));
    }

    /**
     * @param diagramsLocation the directory holding the images of all projects
     * @param diagram          a diagram
     * @return where the diagram's image was stored before images were named after the diagram's id
     */
    public static Path legacyImagePath(String diagramsLocation, Diagram diagram) {
        return Path.of(diagramsLocation, diagram.getProject().getId().toString(), diagram.getName() + SVG_FILE_SUFFIX);
    }

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
