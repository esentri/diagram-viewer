package io.domainlifecycles.diagramviewer.webapp.components.various.filtering;

import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;

public class DiagramVisibilityComponentsContainer extends VerticalLayout {

    private DiagramFilterComponent diagramFilterComponent;
    private DiagramVisibilityComponent diagramVisibilityComponent;

    public DiagramVisibilityComponentsContainer(
            SessionStorage sessionStorage,
            DiagramService diagramService
    ) {

        setWidthFull();
        this.diagramFilterComponent = new DiagramFilterComponent(sessionStorage, diagramService);
        this.diagramVisibilityComponent = new DiagramVisibilityComponent(sessionStorage, diagramService);
        add(this.diagramFilterComponent,
            new Hr(),
            this.diagramVisibilityComponent);
    }

    public void setDiagram(Diagram diagram) {
        this.diagramVisibilityComponent.setDiagram(diagram);
        this.diagramFilterComponent.setDiagram(diagram);
    }
}
