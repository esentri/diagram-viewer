package io.domainlifecycles.diagramviewer.webapp.components.various.cards;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.RouteParameters;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.util.DiagramFileUtils;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramView;
import java.util.Map;

public class DiagramCardGrid extends FormLayout {

    private final Project project;

    public DiagramCardGrid(Project project) {
        this.project = project;

        setClassName("diagram-card-grid");
        setSizeFull();
        setResponsiveSteps(new ResponsiveStep("0", 5));
        buildGrid();
    }

    private void buildGrid() {
        project.getDiagrams().forEach(diagram -> {
            DiagramCard diagramCard = new DiagramCard(diagram.getFileName(), DiagramFileUtils.assembleDiagramUrl(project.getProjectNameClean(), diagram.getFileName()));

            Div diagramCardLinkWrapper = new Div(diagramCard);
            diagramCardLinkWrapper.addClickListener(event -> UI.getCurrent().navigate(DiagramView.class, new RouteParameters(
                Map.of("projectName", project.getProjectNameClean(), "diagramName",
                    diagram.getFileName()))));
            diagramCardLinkWrapper.getStyle().set("cursor", "pointer");

            add(diagramCardLinkWrapper);
        });
    }
}
