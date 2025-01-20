package io.domainlifecycles.diagramviewer.views;

import com.flowingcode.vaadin.addons.zoomist.Zoomist;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.domainlifecycles.diagramviewer.layout.MainView;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;

@Route(value = "/", layout = MainView.class)
@PageTitle("DLC | Diagram Viewer")
public class DiagramViewerView extends VerticalLayout {

    @Value("${diagrams.location}")
    private String diagramFolderLocation;
    private final TabSheet tabSheet;

    public DiagramViewerView() {
        this.setSizeFull();

        tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        add(tabSheet);
    }

    // PostConstruct so property is evaluated
   @PostConstruct
   public void addTabs() {
       Set<String> diagramFileNames = listFilesInDiagramDirectory();

       diagramFileNames.forEach(diagramFileName -> {
           Zoomist zoomist = getZoomist(diagramFileName);

           FlexLayout zoomistContainer = new FlexLayout();
           zoomistContainer.setFlexGrow(1, zoomist);
           zoomistContainer.add(zoomist);
           zoomistContainer.setMinHeight("0%");

           tabSheet.add(diagramFileName, zoomistContainer);
       });
   }

    private Zoomist getZoomist(String imageSrc) {
        Zoomist zoomist = new Zoomist("images/" + imageSrc);
        zoomist.setZoomer(true);
        zoomist.setBounds(false);
        zoomist.setDraggable(true);
        return zoomist;
    }

    private Set<String> listFilesInDiagramDirectory() {
        return Stream.of(Objects.requireNonNull(new File(diagramFolderLocation).listFiles()))
            .filter(file -> !file.isDirectory())
            .map(File::getName)
            .collect(Collectors.toSet());
    }
}
