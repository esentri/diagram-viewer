package io.domainlifecycles.diagramviewer.rest.api;

import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.mirror.api.DomainMirror;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(DomainMirrorUploadController.UPLOAD_DOMAIN_MIRROR_API_PATH)
public class DomainMirrorUploadController {

    public static final String UPLOAD_DOMAIN_MIRROR_API_PATH = "/api/upload/";

    private final ProjectService projectService;

    public DomainMirrorUploadController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PutMapping("/domain-mirror/{projectName}")
    public ResponseEntity<String> createOrUpdateDomainModel(
        @PathVariable String projectName, @RequestBody DomainMirror domainMirror) {

        projectService.createOrUpdateDomainModel(projectName, domainMirror);
        return ResponseEntity.ok().build();
    }
}
