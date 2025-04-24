package io.domainlifecycles.diagramviewer.rest;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.mirror.api.DomainModel;
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
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(DomainModelController.DOMAIN_MODEL_API_PATH)
public class DomainModelController {

    public static final String DOMAIN_MODEL_API_PATH = "/api/domain-model";

    @PostMapping(value = "/{projectName}")
    public ResponseEntity<String> uploadDomainModel(
        @RequestBody DomainModel domainModel,
        @PathVariable("projectName") String projectName) {

        return new ResponseEntity<>(projectName, HttpStatus.OK);
    }
}
