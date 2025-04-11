package io.domainlifecycles.diagramviewer.session;

import io.domainlifecycles.diagramviewer.model.AuthenticatedUser;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.service.AuthenticatedUserService;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.util.DomainModelUtils;
import io.domainlifecycles.mirror.api.DomainModel;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * Holds session values about the initialized Domain models.
 */
@Component("sessionStorage")
@Scope(scopeName = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionStorage.class);

    private final AuthenticatedUserService authenticatedUserService;
    private final ProjectService projectService;
    private final String targetsDirectory;
    private final HashMap<Long, DomainModel> domainModelStore;

    private AuthenticatedUser authenticatedUser;
    private Project selectedProject;
    private Diagram selectedDiagram;


    public SessionStorage(
        @Value("${targets.location}") String targetsDirectory,
        AuthenticatedUserService authenticatedUserService, ProjectService projectService) {

        this.targetsDirectory = targetsDirectory;
        this.authenticatedUserService = authenticatedUserService;
        this.projectService = projectService;
        domainModelStore = new HashMap<>();

        initializeAllDomainModels();
    }

    public AuthenticatedUser getAuthenticatedUser() {
        return authenticatedUser;
    }

    public void setAuthenticatedUser(AuthenticatedUser authenticatedAuthenticatedUser) {
        this.authenticatedUser = authenticatedAuthenticatedUser;
    }

    public void refreshAuthenticatedUser() {
        authenticatedUser = authenticatedUserService.get(authenticatedUser.getEmailAddress());
    }

    public DomainModel get(Long projectId) {
        return domainModelStore.get(projectId);
    }

    public DomainModel add(Project project) {
        final Path pathToTarget = Path.of(project.getAbsolutePathToTarget());
        final DomainModel domainModel = DomainModelUtils.initializeDomainModelFromJar(pathToTarget,
            project.getBoundedContextPackages().toArray(new String[0]));

        domainModelStore.put(project.getId(), domainModel);
        return domainModel;
    }

    public void setNoneSelected() {
        selectedProject = null;
        selectedDiagram = null;
    }

    public void setNoDiagramSelected() {
        selectedProject = null;
    }

    public Project getSelectedProject() {
        return selectedProject;
    }

    public void setSelectedProject(Project selectedProject) {
        this.selectedProject = selectedProject;
    }

    public Diagram getSelectedDiagram() {
        return selectedDiagram;
    }

    public void setSelectedDiagram(Diagram selectedDiagram) {
        this.selectedDiagram = selectedDiagram;
    }

    public String getTargetsLocation() {
        return targetsDirectory;
    }

    public boolean isDiagramSelected() {
        return selectedDiagram != null;
    }

    private void initializeAllDomainModels(){
        if (targetsDirectory != null) {
            File dir = new File(targetsDirectory);

            if(dir.exists()) {
                projectService.getAll(dir.toPath(), authenticatedUser).forEach(this::add);
            }
        }
    }
}
