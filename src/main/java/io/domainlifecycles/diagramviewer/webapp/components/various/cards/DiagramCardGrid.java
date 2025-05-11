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
                DiagramCard diagramCard = new DiagramCard(diagram,
                    DiagramFileUtils.assembleDiagramUrl(
                        diagram.getChangedAt(), diagram.getDiagramStylingConfiguration().getChangedAt(),
                        project.getId().toString(), diagram.getFileName()));

                Div diagramCardLinkWrapper = new Div(diagramCard);
                diagramCardLinkWrapper.addClickListener(
                    event -> UI.getCurrent().navigate(DiagramView.class, new RouteParameters(
                        Map.of(ProjectView.PROJECT_NAME_ROUTE_PARAMETER, project.getName(),
                            DiagramView.DIAGRAM_NAME_ROUTE_PARAMETER, diagram.getFileName()))));
                diagramCardLinkWrapper.getStyle().set("cursor", "pointer");

                diagramCard.setDragData(diagram);
                diagramCard.setDropEffect(DropEffect.COPY);

                diagramCard.addDropListener(event -> {
                    String draggedDiagramName = ((Diagram) event.getDragData().orElseThrow()).getFileName();
                    Notification.show("Dropped '" + draggedDiagramName + "' on '" + diagram.getFileName() + "'.");
                });

                diagramCard.addDragStartListener(event -> diagramCard.setActive(false));
                diagramCard.addDragEndListener(event -> diagramCard.setActive(true));

                add(diagramCardLinkWrapper);
            });
    }

    private int getComponentIndex(Component component) {
        if (component.getParent().isEmpty()) return -1;
        Component parent = component.getParent().get();
        return parent.getChildren().toList().indexOf(component);
    }
}
