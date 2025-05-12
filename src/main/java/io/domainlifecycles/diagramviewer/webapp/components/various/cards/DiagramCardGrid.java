package io.domainlifecycles.diagramviewer.webapp.components.various.cards;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.RouteParameters;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.util.DiagramFileUtils;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramView;
import io.domainlifecycles.diagramviewer.webapp.views.ProjectView;
import java.util.Comparator;
import java.util.Map;

public class DiagramCardGrid extends FormLayout {

    private final Project project;

    public DiagramCardGrid(Project project) {
        this.project = project;

        setSizeFull();
        getStyle().setMarginTop("2rem");
        setResponsiveSteps(
            new ResponsiveStep("300px", 2),
            new ResponsiveStep("600px", 3),
            new ResponsiveStep("900px", 4),
            new ResponsiveStep("1200px", 5)
        );
        buildGrid();
    }

    private void buildGrid() {
        project.getDiagrams()
            .stream()
            .sorted(Comparator.comparing(Diagram::getCreatedAt))
            .forEach(diagram -> {
                DiagramCardLinkWrapper diagramCardLinkWrapper = new DiagramCardLinkWrapper(project, diagram);
                add(diagramCardLinkWrapper);
            });
    }
}
