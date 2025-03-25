package io.domainlifecycles.diagramviewer.session;

import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.files.DirectoryWatcher;
import io.domainlifecycles.diagramviewer.kroki.FileType;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.util.DiagrammerUtils;
import io.domainlifecycles.diagramviewer.util.DomainModelUtils;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.layout.MainLayout;
import io.domainlifecycles.mirror.api.DomainModel;
import io.domainlifecycles.mirror.serialize.api.JacksonDomainSerializer;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import jdk.jshell.Diag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * Holds session values about the initialized Domain models.
 */
@SpringComponent
public class DomainModelSessionStorage {

    private static final Logger log = LoggerFactory.getLogger(DomainModelSessionStorage.class);

    private MainLayout mainLayout;
    private Project selectedProject;
    private Diagram selectedDiagram;
    private String selectedTargetsDirectory;
    private final HashMap<Long, DomainModel> domainModelStore;

    public DomainModelSessionStorage(@Value("${targets.location}") String defaultTargetsLocation) {
        domainModelStore = new HashMap<>();
        this.selectedTargetsDirectory = defaultTargetsLocation;
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

    public void purge() {
        domainModelStore.clear();
        setNoneSelected();
    }

    public void setNoneSelected() {
        selectedProject = null;
        selectedDiagram = null;
        if(mainLayout != null) mainLayout.updateDownloadLink(false);
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
        if(mainLayout != null) mainLayout.updateDownloadLink(true);
    }

    public String getSelectedTargetsDirectory() {
        return selectedTargetsDirectory;
    }

    public boolean isDiagramSelected() {
        return selectedDiagram != null;
    }

    public void setMainLayout(MainLayout mainLayout) {
        this.mainLayout = mainLayout;
    }
}
