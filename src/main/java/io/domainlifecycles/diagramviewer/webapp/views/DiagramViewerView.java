package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.files.DirectoryWatcher;
import io.domainlifecycles.diagramviewer.generate.SQLDDLGeneratorService;
import io.domainlifecycles.diagramviewer.session.AnalyzedDomainModel;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.DiagramConfigurationButtonBarComponent;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.DiagramTabSheetComponent;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.DiagramZoomComponent;
import io.domainlifecycles.diagramviewer.webapp.layout.MainView;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

@Route(value = "/diagrams", layout = MainView.class)
@PageTitle("DLC | Diagram Viewer")
public class DiagramViewerView extends FlexLayout implements BeforeEnterObserver {

    private final static Logger log = LoggerFactory.getLogger(DiagramViewerView.class);

    private String projectName;
    private String diagramName;
    private final String diagramsDirectory;

    public DiagramViewerView(@Value("${diagrams.location}") String diagramsDirectory,
                             AnalyzedDomainModel analyzedDomainModel,
                             SQLDDLGeneratorService sqlddlGeneratorService) {

        this.diagramsDirectory = diagramsDirectory;
        setSizeFull();
        setClassName("diagram-viewer");

        add(new DiagramConfigurationButtonBarComponent(sqlddlGeneratorService, analyzedDomainModel));
        add(new DiagramZoomComponent(Path.of(diagramsDirectory, diagramName).toString()));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        projectName = event.getRouteParameters().get("projectName").get();
        diagramName = event.getRouteParameters().get("diagramName").get();
    }
}
