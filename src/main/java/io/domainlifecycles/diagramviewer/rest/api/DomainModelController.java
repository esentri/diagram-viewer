package io.domainlifecycles.diagramviewer.rest.api;

import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.mirror.api.DomainModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(DomainModelController.DOMAIN_MODEL_API_PATH)
public class DomainModelController {

    public static final String DOMAIN_MODEL_API_PATH = "/api/domain-model/";

    private final ProjectService projectService;

    public DomainModelController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PutMapping("/{projectName}")
    public ResponseEntity<String> createOrUpdateDomainModel(@PathVariable String projectName, @RequestBody DomainModel domainModel) {
        projectService.createOrUpdateDomainModel(projectName, domainModel);
        return ResponseEntity.ok().build();
    }
}
