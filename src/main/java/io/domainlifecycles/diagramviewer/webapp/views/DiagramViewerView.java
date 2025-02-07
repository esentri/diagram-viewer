package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.files.DirectoryWatcher;
import io.domainlifecycles.diagramviewer.generate.SQLDDLGeneratorService;
import io.domainlifecycles.diagramviewer.session.AnalyzedDomainModel;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.DiagramConfigurationButtonBarComponent;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.DiagramTabSheetComponent;
import io.domainlifecycles.diagramviewer.webapp.layout.MainView;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

@Route(value = "/", layout = MainView.class)
@PageTitle("DLC | Diagram Viewer")
public class DiagramViewerView extends FlexLayout implements VaadinServiceInitListener {

    private final static Logger log = LoggerFactory.getLogger(DiagramViewerView.class);

    private final String diagramDirectory;
    private final DiagramTabSheetComponent tabSheet;

    public DiagramViewerView(@Value("${diagrams.location}") String diagramDirectory,
                             AnalyzedDomainModel analyzedDomainModel,
                             SQLDDLGeneratorService sqlddlGeneratorService) {

        this.diagramDirectory = diagramDirectory;
        setSizeFull();
        setClassName("diagram-viewer");

        add(new DiagramConfigurationButtonBarComponent(sqlddlGeneratorService, analyzedDomainModel));
        tabSheet = new DiagramTabSheetComponent();
        add(tabSheet);

        initWatcherService();
        refreshTabs();
    }

    //TODO schauen ob weiterhin notwendig
    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addSessionInitListener(
                initEvent -> {
                    log.info("A new Session has been initialized!");
                });

        event.getSource().addUIInitListener(
                initEvent -> log.info("A new UI has been initialized!"));
    }

    public void initWatcherService() {
        Path directoryToWatch;

        try {
            directoryToWatch = Path.of(diagramDirectory);
        } catch (InvalidPathException e) {
            throw DiagramViewerException.fail(
                String.format("Specified path '%s' is not a directory.", diagramDirectory), e);
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
        Set<String> diagramFileNames = FileIOUtils.getFileNamesInDiagramDirectory(diagramDirectory);
        tabSheet.addTabs(diagramFileNames);
    }

    private void removeAllTabsFromTabSheet() {
        int tabCount = tabSheet.getTabCount();
        for(int i = 0; i < tabCount; i++) {
            tabSheet.remove(0);
        }
    }
}
