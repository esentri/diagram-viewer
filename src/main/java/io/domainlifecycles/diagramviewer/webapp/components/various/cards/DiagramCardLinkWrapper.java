package io.domainlifecycles.diagramviewer.webapp.components.various.cards;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.RouteParameters;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.util.DiagramFileUtils;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramView;
import io.domainlifecycles.diagramviewer.webapp.views.ProjectView;
import java.util.Map;

public class DiagramCardLinkWrapper extends Div {

    public DiagramCardLinkWrapper(Project project, Diagram diagram) {
        getStyle().set("cursor", "pointer");
        getStyle().setMarginBottom("calc(var(--vaadin-form-layout-column-spacing))");

        addClickListener(
            event -> UI.getCurrent().navigate(DiagramView.class, new RouteParameters(
                Map.of(ProjectView.PROJECT_NAME_ROUTE_PARAMETER, project.getName(),
                    DiagramView.DIAGRAM_NAME_ROUTE_PARAMETER, diagram.getFileName()))));

        add(createAndGetDiagramCard(project, diagram));
    }

    private DiagramCard createAndGetDiagramCard(Project project, Diagram diagram) {
        return new DiagramCard(diagram,
            DiagramFileUtils.assembleDiagramUrl(
                diagram.getChangedAt(), diagram.getDiagramStylingConfiguration().getChangedAt(),
                project.getId().toString(), diagram.getFileName()));
    }
}
