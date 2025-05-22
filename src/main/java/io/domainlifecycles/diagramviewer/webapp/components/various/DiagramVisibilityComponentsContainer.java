package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.util.List;

public class DiagramVisibilityComponentsContainer extends VerticalLayout {

    public DiagramVisibilityComponentsContainer(SessionStorage sessionStorage, Project project, Diagram diagram, List<DomainTypeMirror> domainTypeMirrors,
                                                DiagramService diagramService) {
        setWidthFull();
        add(new DiagramFilterComponent(sessionStorage, diagram, project, domainTypeMirrors, diagramService),
            new Hr(),
            new DiagramVisibilityComponent(sessionStorage, project, diagram, domainTypeMirrors, diagramService));
    }
}
