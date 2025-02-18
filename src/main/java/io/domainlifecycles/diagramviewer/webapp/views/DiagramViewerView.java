package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.domainlifecycles.diagramviewer.rest.ResourceController;
import io.domainlifecycles.diagramviewer.session.AnalyzedDomainModel;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramConfigurationButtonBarComponent;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramZoomComponent;
import io.domainlifecycles.diagramviewer.webapp.layout.MainView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route(value = "/:projectName/:diagramName", layout = MainView.class)
@PageTitle("DLC | Diagram Viewer")
public class DiagramViewerView extends FlexLayout implements BeforeEnterObserver {

    private final static Logger log = LoggerFactory.getLogger(DiagramViewerView.class);

    private String projectName;
    private String diagramName;
    private FlexLayout zoomComponentContainer;

    public DiagramViewerView(AnalyzedDomainModel analyzedDomainModel) {
        setSizeFull();
        setClassName("diagram-viewer");

        add(new DiagramConfigurationButtonBarComponent(analyzedDomainModel));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        projectName = event.getRouteParameters().get("projectName").get();
        diagramName = event.getRouteParameters().get("diagramName").get();

        addDiagramZoomComponent();
    }

    private void addDiagramZoomComponent() {
        if(zoomComponentContainer != null) remove(zoomComponentContainer);

        DiagramZoomComponent diagramZoomComponent = new DiagramZoomComponent(
            ResourceController.RESOURCES_API_PATH, projectName, diagramName);

        zoomComponentContainer = new FlexLayout();
        zoomComponentContainer.setClassName("zoomist-container");
        zoomComponentContainer.setFlexGrow(1, diagramZoomComponent);
        zoomComponentContainer.add(diagramZoomComponent);
        zoomComponentContainer.setMaxHeight("100%");
        zoomComponentContainer.setSizeFull();
        add(zoomComponentContainer);
    }
}
