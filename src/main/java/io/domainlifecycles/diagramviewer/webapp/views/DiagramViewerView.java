package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.domainlifecycles.diagramviewer.rest.ResourceController;
import io.domainlifecycles.diagramviewer.session.AnalyzedDomainModel;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramConfigurationButtonBarComponent;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramVisibilityAccordionComponent;
import io.domainlifecycles.diagramviewer.webapp.components.various.DiagramZoomComponent;
import io.domainlifecycles.diagramviewer.webapp.layout.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.util.stream.Collectors.groupingBy;

@Route(value = "/:projectName/:diagramName", layout = MainLayout.class)
@PageTitle("DLC | Diagram Viewer")
public class DiagramViewerView extends FlexLayout implements BeforeEnterObserver {

    private final static Logger log = LoggerFactory.getLogger(DiagramViewerView.class);

    private final AnalyzedDomainModel analyzedDomainModel;
    private String projectNameClean;
    private String diagramName;
    private FlexLayout zoomComponentContainer;
    private DiagramZoomComponent diagramZoomComponent;

    public DiagramViewerView(AnalyzedDomainModel analyzedDomainModel) {
        this.analyzedDomainModel = analyzedDomainModel;
        setSizeFull();
        setClassName("diagram-viewer");

        add(new DiagramConfigurationButtonBarComponent(analyzedDomainModel));
        add(createDiagramZoomComponent());
        add(new DiagramVisibilityAccordionComponent(analyzedDomainModel, this::reloadDiagramZoomComponent));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        projectNameClean = event.getRouteParameters().get("projectName").get();
        diagramName = event.getRouteParameters().get("diagramName").get();

        reloadDiagramZoomComponent();
    }

    private FlexLayout createDiagramZoomComponent() {
        if(zoomComponentContainer != null) remove(zoomComponentContainer);

        zoomComponentContainer = new FlexLayout();
        zoomComponentContainer.setClassName("zoomist-container");
        zoomComponentContainer.setMaxHeight("100%");
        zoomComponentContainer.setSizeFull();
        return zoomComponentContainer;
    }

    private void reloadDiagramZoomComponent() {
        if(diagramZoomComponent != null) {
            zoomComponentContainer.remove(diagramZoomComponent);
        }

        diagramZoomComponent = new DiagramZoomComponent(
            ResourceController.RESOURCES_API_PATH, projectNameClean, diagramName);

        zoomComponentContainer.setFlexGrow(1, diagramZoomComponent);
        zoomComponentContainer.add(diagramZoomComponent);
    }
}
