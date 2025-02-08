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
import io.domainlifecycles.diagramviewer.webapp.layout.MainView;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

@Route(value = "/:targetName", layout = MainView.class)
@PageTitle("DLC | Diagram Viewer")
public class DiagramViewerView extends FlexLayout implements BeforeEnterObserver {

    private final static Logger log = LoggerFactory.getLogger(DiagramViewerView.class);

    private String projectName;
    private final String diagramsDirectory;
    private final DiagramTabSheetComponent tabSheet;

    public DiagramViewerView(@Value("${diagrams.location}") String diagramsDirectory,
                             AnalyzedDomainModel analyzedDomainModel,
                             SQLDDLGeneratorService sqlddlGeneratorService) {

        this.diagramsDirectory = diagramsDirectory;
        setSizeFull();
        setClassName("diagram-viewer");

        add(new DiagramConfigurationButtonBarComponent(sqlddlGeneratorService, analyzedDomainModel));
        tabSheet = new DiagramTabSheetComponent();
        add(tabSheet);
    }

    public void initWatcherService() {
        Path directoryToWatch;

        try {
            directoryToWatch = Path.of(diagramsDirectory, projectName);
        } catch (InvalidPathException e) {
            throw DiagramViewerException.fail(
                String.format("Specified path '%s' is not a directory.", diagramsDirectory), e);
        }

        DirectoryWatcher.onDirectoryChange(directoryToWatch,
            (evt) -> {
                log.debug("Noticed change in watched directory. Refreshing tabs.");
                this.getUI().ifPresent(ui -> ui.access(this::refreshTabs));
            }
        );
    }

    public void refreshTabs() {
        removeAllTabsFromTabSheet();
        Set<String> diagramFileNames = FileIOUtils.getFileNamesInDirectory(diagramsDirectory);
        tabSheet.addTabs(diagramFileNames);
    }

    private void removeAllTabsFromTabSheet() {
        int tabCount = tabSheet.getTabCount();
        for(int i = 0; i < tabCount; i++) {
            tabSheet.remove(0);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        projectName = event.getRouteParameters().get("targetName").get();

        initWatcherService();
        refreshTabs();
    }
}
