package io.domainlifecycles.diagramviewer.webapp.components.various.cards;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.RouteParameters;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.DiagramDirectoryService;
import io.domainlifecycles.diagramviewer.util.DiagramFileUtils;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramDirectoryView;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramView;
import io.domainlifecycles.diagramviewer.webapp.views.ProjectView;
import java.util.Map;

public class CardLinkWrapper extends Div {

    private CardLinkWrapper() {
        getStyle().set("cursor", "pointer");
        getStyle().setMarginBottom("calc(var(--vaadin-form-layout-column-spacing))");
    }

    public CardLinkWrapper(DiagramDirectoryService diagramDirectoryService, Project project, Diagram diagram) {
        this();
        addClickListener(
            event -> UI.getCurrent().navigate(DiagramView.class, new RouteParameters(
                Map.of(ProjectView.PROJECT_NAME_ROUTE_PARAMETER, project.getName(),
                    DiagramView.DIAGRAM_NAME_ROUTE_PARAMETER, diagram.getFileName()))));

        add(createAndGetDiagramCard(diagramDirectoryService, project, diagram));
    }

    public CardLinkWrapper(DiagramDirectoryService diagramDirectoryService, Project project, DiagramDirectory diagramDirectory) {
        this();
        addClickListener(
            event -> UI.getCurrent().navigate(DiagramDirectoryView.class, new RouteParameters(
                Map.of(DiagramDirectoryView.DIAGRAM_DIRECTORY_NAME_ROUTE_PARAMETER, diagramDirectory.getName()))));

        add(createAndGetDiagramCard(diagramDirectoryService, project, diagramDirectory));
    }

    private DiagramCard createAndGetDiagramCard(DiagramDirectoryService diagramDirectoryService, Project project, Diagram diagram) {
        return new DiagramCard(diagramDirectoryService, diagram,
            DiagramFileUtils.assembleDiagramUrl(
                diagram.getChangedAt(), diagram.getDiagramStylingConfiguration().getChangedAt(),
                project.getId().toString(), diagram.getFileName()));
    }

    private DiagramCard createAndGetDiagramCard(DiagramDirectoryService diagramDirectoryService, Project project, DiagramDirectory diagramDirectory) {
        return new DiagramCard(diagramDirectoryService, diagramDirectory);
    }
}
