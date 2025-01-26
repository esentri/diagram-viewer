package io.domainlifecycles.diagramviewer.rest;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ResourceController.RESOURCES_API_PATH)
public class ResourceController {

    public static final String RESOURCES_API_PATH = "/api/resources";

    public static final String TIMESTAMP_REQUEST_PARAMETER_NAME = "timestamp";

    @Value("${diagrams.location}")
    private String diagramFolderLocation;

    @GetMapping(value = "/{fileName}")
    public ResponseEntity<InputStreamResource> getFile(
            @PathVariable("fileName") String fileName,
            @RequestParam(TIMESTAMP_REQUEST_PARAMETER_NAME) String ignored) throws IOException {

        URI filePath = Path.of(diagramFolderLocation, fileName).toUri();
        InputStream inputStream = new FileInputStream(new File(filePath));
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

        if(!inputStreamResource.exists()) {
            throw DiagramViewerException.fail(
                String.format("Could not find file %s in directory %s.", fileName, diagramFolderLocation));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", evaluateContentType(fileName));
        headers.setContentLength(Files.size(Paths.get(filePath)));
        return new ResponseEntity<>(inputStreamResource, headers, HttpStatus.OK);
    }

    private String evaluateContentType(String fileName) {
        Optional<MediaType> mimeTypeOptional = MediaTypeFactory.getMediaType(fileName);
        return mimeTypeOptional.map(MediaType::toString).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }
}
