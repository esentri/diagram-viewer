package io.domainlifecycles.diagramviewer.rest.api;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    public static final String DIAGRAM_LAST_MODIFIED_REQUEST_PARAMETER_NAME = "ignored1";
    public static final String STYLING_LAST_MODIFIED_REQUEST_PARAMETER_NAME = "stylingLastModified";


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

        URI filePath;
        try {
            filePath = Path.of(diagramFolderLocation, directoryName, fileName).toUri();
        } catch (InvalidPathException | IOError e) {
            throw DiagramViewerException.fail(
                String.format("Location of requested file '%s/%s/%s' is not a valid path.",
                    diagramFolderLocation, directoryName, fileName));
        }

        InputStream inputStream;
        try {
            inputStream = new FileInputStream(new File(filePath));
        } catch(NullPointerException e) {
            throw DiagramViewerException.fail("No path specified for requested file.", e);
        } catch (FileNotFoundException e) {
            throw DiagramViewerException.fail(String.format("No file found at '%s'.", filePath.getPath()), e);
        }

        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

        if(!inputStreamResource.exists()) {
            throw DiagramViewerException.fail(
                String.format("Could not find file %s in directory %s/%s.", fileName, diagramFolderLocation, directoryName));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(evaluateContentType(fileName));
        headers.setContentLength(Files.size(Paths.get(filePath)));
        headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(30)));
        return new ResponseEntity<>(inputStreamResource, headers, HttpStatus.OK);
    }

    private MediaType evaluateContentType(String fileName) {
        Optional<MediaType> mimeTypeOptional = MediaTypeFactory.getMediaType(fileName);
        return mimeTypeOptional.orElse(MediaType.APPLICATION_OCTET_STREAM);
    }
}
