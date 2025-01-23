package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.DiagramConfigurationButtonBarComponent;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.DiagramTabSheetComponent;
import io.domainlifecycles.diagramviewer.webapp.layout.MainView;
import io.domainlifecycles.diagramviewer.files.DirectoryMonitorService;
import jakarta.annotation.PostConstruct;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@Route(value = "/", layout = MainView.class)
@PageTitle("DLC | Diagram Viewer")
public class DiagramViewerView extends FlexLayout {

    @Value("${diagrams.location}")
    private String diagramDirectory;
    private final DiagramTabSheetComponent tabSheet;

    private final DirectoryMonitorService directoryMonitorService;

    @Autowired
    public DiagramViewerView(DirectoryMonitorService directoryMonitorService) {
        this.directoryMonitorService = directoryMonitorService;

        setSizeFull();
        setClassName("diagram-viewer");

        add(new DiagramConfigurationButtonBarComponent());

        tabSheet = new DiagramTabSheetComponent();
        add(tabSheet);
    }

    // PostConstruct so property is evaluated
    @PostConstruct
    public void startDirectoryMonitoring() {
        directoryMonitorService.startMonitoring(
            diagramDirectory, () -> getUI().ifPresent(ui -> ui.getPage().reload()));
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
            tabSheet.remove(i);
        }
    }
}
