package io.domainlifecycles.diagramviewer.session;

import com.vaadin.flow.spring.annotation.SpringComponent;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.User;
import io.domainlifecycles.diagramviewer.util.DomainModelUtils;
import io.domainlifecycles.mirror.api.DomainModel;
import java.nio.file.Path;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * Holds session values about the initialized Domain models.
 */
@SpringComponent
public class DomainModelSessionStorage {

    private static final Logger log = LoggerFactory.getLogger(DomainModelSessionStorage.class);

    private User authenticatedUser;
    private Project selectedProject;
    private Diagram selectedDiagram;
    private final String targetsLocation;
    private final HashMap<Long, DomainModel> domainModelStore;

    public DomainModelSessionStorage(@Value("${targets.location}") String targetsLocation) {
        domainModelStore = new HashMap<>();
        this.targetsLocation = targetsLocation;
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    public void setAuthenticatedUser(User authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    public DomainModel get(Long projectId) {
        return domainModelStore.get(projectId);
    }

    public DomainModel add(Project project) {
        final Path pathToTarget = Path.of(project.getAbsolutePathToTarget());
        final DomainModel domainModel = DomainModelUtils.initializeDomainModelFromJar(pathToTarget,
            project.getBoundedContextPackages().toArray(new String[0]));

        domainModelStore.put(project.getProjectId(), domainModel);
        return domainModel;
    }

    public void setNoneSelected() {
        selectedProject = null;
        selectedDiagram = null;
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
        return targetsLocation;
    }

    public boolean isDiagramSelected() {
        return selectedDiagram != null;
    }

}
