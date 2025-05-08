package io.domainlifecycles.diagramviewer.rest.api;

import io.domainlifecycles.diagramviewer.rest.api.model.DomainMirrorUploadDto;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(DomainMirrorController.DOMAIN_MIRROR_API_PATH)
public class DomainMirrorController {

    public static final String DOMAIN_MIRROR_API_PATH = "/api/domain-mirror/";

    private final ProjectService projectService;

    public DomainMirrorController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PutMapping("/{projectName}")
    public ResponseEntity<String> createOrUpdateDomainModel(
        @PathVariable String projectName, @RequestBody DomainMirrorUploadDto domainMirrorUploadDto) {

        projectService.createOrUpdateDomainMirror(projectName, domainMirrorUploadDto);
        return ResponseEntity.ok().build();
    }
}
