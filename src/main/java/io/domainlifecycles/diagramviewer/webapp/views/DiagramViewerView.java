package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.domainlifecycles.diagramviewer.files.FileWatcher;
import io.domainlifecycles.diagramviewer.jar.JarToDiagramService;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.util.MirrorUtils;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.DiagramConfigurationButtonBarComponent;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.DiagramTabSheetComponent;
import io.domainlifecycles.diagramviewer.webapp.layout.MainView;
import jakarta.annotation.PostConstruct;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@Route(value = "/", layout = MainView.class)
@PageTitle("DLC | Diagram Viewer")
public class DiagramViewerView extends FlexLayout {

    private final static Logger log = LoggerFactory.getLogger(DiagramViewerView.class);

    @Value("${diagrams.location}")
    private String diagramDirectory;

    @Value("${targets.location}")
    private String targetsDirectory;

    @Autowired
    private JarToDiagramService jarToDiagramService;

    private final DiagramTabSheetComponent tabSheet;

    public DiagramViewerView() {
        setSizeFull();
        setClassName("diagram-viewer");

        add(new DiagramConfigurationButtonBarComponent());

        tabSheet = new DiagramTabSheetComponent();
        add(tabSheet);
    }

    // PostConstruct so property is evaluated
    @PostConstruct
    public void initWatcherService() throws IOException {
        initTargets();
        FileWatcher.onFileChange(Path.of(diagramDirectory),
            (evt) -> this.getUI().ifPresent(ui -> ui.access(this::refreshTabs)));
        FileWatcher.onFileChange(Path.of(targetsDirectory),
                (evt) -> jarToDiagramService.createDiagramFromJar(Path.of(targetsDirectory,evt.context().toString()), "com.esentri"));
    }

    private void initTargets(){
        File dir = new File(targetsDirectory);
        if(dir.exists()){
            var files = dir.listFiles();
            if(files.length > 0){
                jarToDiagramService.createDiagramFromJar(files[0].toPath(), "com.esentri");
            }
            if(files.length > 1){
                log.warn("Only first target initialized currently!");
            }
        }
    }

    // PostConstruct so property is evaluated
    @PostConstruct
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
