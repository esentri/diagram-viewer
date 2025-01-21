package io.domainlifecycles.diagramviewer.webapp.components.viewer;

import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import io.domainlifecycles.diagramviewer.rest.ResourceController;
import java.util.Set;

public class DiagramTabSheetComponent extends TabSheet {

    public DiagramTabSheetComponent() {
        setSizeFull();
    }

    public void addTabs(Set<String> diagramFileNames) {
        diagramFileNames.forEach(diagramFileName -> {
            DiagramZoomComponent zoomComponent =
                new DiagramZoomComponent(ResourceController.RESOURCES_API_PATH + "/" + diagramFileName);

            FlexLayout zoomistContainer = generateZoomComponentContainer(zoomComponent);
            add(diagramFileName, zoomistContainer);
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
