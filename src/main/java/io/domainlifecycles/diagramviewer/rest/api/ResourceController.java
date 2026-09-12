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

package io.domainlifecycles.diagramviewer.rest.api;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ResourceController.RESOURCES_API_PATH)
public class ResourceController {

    public static final String RESOURCES_API_PATH = "/api/resources";
    public static final String VIEW_API_PATH_SUFFIX = "/view";
    public static final String DIAGRAM_LAST_MODIFIED_REQUEST_PARAMETER_NAME = "diagramLastModified";
    public static final String STYLING_LAST_MODIFIED_REQUEST_PARAMETER_NAME = "stylingLastModified";
    public static final String NOTES_LAST_MODIFIED_REQUEST_PARAMETER_NAME = "notesLastModified";

    private final String diagramFolderLocation;

    public ResourceController(@Value("${diagrams.location}") String diagramFolderLocation) {
        this.diagramFolderLocation = diagramFolderLocation;
    }

    @GetMapping(value = "/{directoryName}/{fileName}")
    public ResponseEntity<InputStreamResource> getFile(
            @PathVariable("directoryName") String directoryName,
            @PathVariable("fileName") String fileName,
            @RequestParam(DIAGRAM_LAST_MODIFIED_REQUEST_PARAMETER_NAME) String ignored1,
            @RequestParam(STYLING_LAST_MODIFIED_REQUEST_PARAMETER_NAME) String ignored2) throws IOException {

        return getDiagramResponseEntity(directoryName, fileName);
    }

    @GetMapping(value = VIEW_API_PATH_SUFFIX + "/{directoryName}/{fileName}")
    public ResponseEntity<InputStreamResource> getFile(
        @PathVariable("directoryName") String directoryName,
        @PathVariable("fileName") String fileName) throws IOException {

        return getDiagramResponseEntity(directoryName, fileName);
    }

    private ResponseEntity<InputStreamResource> getDiagramResponseEntity(String directoryName, String fileName) throws IOException {
        Path baseDir;
        Path resolvedPath;
        try {
            baseDir = Path.of(diagramFolderLocation).toAbsolutePath().normalize();
            resolvedPath = baseDir.resolve(directoryName).resolve(fileName).normalize();
        } catch (InvalidPathException | IOError e) {
            throw DiagramViewerException.fail(
                String.format("Location of requested file '%s/%s/%s' is not a valid path.",
                    diagramFolderLocation, directoryName, fileName));
        }

        // reject any directoryName/fileName that (after resolving ".." segments or an absolute
        // override) would escape the diagrams directory, e.g. "../../etc/passwd"
        if (!resolvedPath.startsWith(baseDir)) {
            throw DiagramViewerException.fail(
                String.format("Requested file '%s/%s' is outside the diagrams directory.", directoryName, fileName));
        }

        InputStream inputStream;
        try {
            inputStream = new FileInputStream(resolvedPath.toFile());
        } catch (FileNotFoundException e) {
            throw DiagramViewerException.fail(String.format("No file found at '%s'.", resolvedPath), e);
        }

        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

        if(!inputStreamResource.exists()) {
            throw DiagramViewerException.fail(
                String.format("Could not find file %s in directory %s/%s.", fileName, diagramFolderLocation,
                    directoryName));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(evaluateContentType(fileName));
        headers.setContentLength(Files.size(resolvedPath));
        headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(30)));
        return new ResponseEntity<>(inputStreamResource, headers, HttpStatus.OK);
    }

    private MediaType evaluateContentType(String fileName) {
        Optional<MediaType> mimeTypeOptional = MediaTypeFactory.getMediaType(fileName);
        return mimeTypeOptional.orElse(MediaType.APPLICATION_OCTET_STREAM);
    }
}
