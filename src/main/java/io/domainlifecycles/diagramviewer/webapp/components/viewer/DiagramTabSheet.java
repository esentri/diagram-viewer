package io.domainlifecycles.diagramviewer.webapp.components.viewer;

import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import io.domainlifecycles.diagramviewer.rest.ResourceController;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import jakarta.annotation.PostConstruct;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;

public class DiagramTabSheet extends FlexLayout {

    @Value("${diagrams.location}")
    private String diagramDirectory;

    private final TabSheet diagramTabSheet;

    public DiagramTabSheet() {
        setSizeFull();
        diagramTabSheet = new TabSheet();
    }

    // PostConstruct so property is evaluated
    @PostConstruct
    public void addTabs() {
        Set<String> diagramFileNames = FileIOUtils.getFileNamesInDiagramDirectory(diagramDirectory);

        diagramFileNames.forEach(diagramFileName -> {
            DiagramZoomComponent zoomComponent =
                new DiagramZoomComponent(ResourceController.RESOURCES_API_PATH + "/" + diagramFileName);

            FlexLayout zoomistContainer = generateZoomComponentContainer(zoomComponent);
            diagramTabSheet.add(diagramFileName, zoomistContainer);
        });
    }

    private FlexLayout generateZoomComponentContainer(final DiagramZoomComponent zoomComponent) {
        FlexLayout zoomistContainer = new FlexLayout();
        zoomistContainer.setFlexGrow(1, zoomComponent);
        zoomistContainer.add(zoomComponent);
        zoomistContainer.setMinHeight("0%");
        return zoomistContainer;
    }
}
