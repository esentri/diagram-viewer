package io.domainlifecycles.diagramviewer.webapp.components.various.cards;

import com.vaadin.flow.component.formlayout.FormLayout;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.DiagramDirectoryService;
import java.util.Comparator;
import java.util.Set;

public class DiagramCardGrid extends FormLayout {

    private DiagramCardGrid() {
        setSizeFull();
        getStyle().setMarginTop("2rem");
        setResponsiveSteps(
            new ResponsiveStep("300px", 2),
            new ResponsiveStep("600px", 3),
            new ResponsiveStep("900px", 4),
            new ResponsiveStep("1200px", 5)
        );
    }

    public DiagramCardGrid(
        DiagramDirectoryService diagramDirectoryService,
        Project project,
        Set<DiagramDirectory> diagramDirectories,
        Set<Diagram> diagrams) {

        this();
        buildGrid(diagramDirectoryService, project, diagramDirectories, diagrams);
    }

    public DiagramCardGrid(
        DiagramDirectoryService diagramDirectoryService,
        Project project,
        Set<Diagram> diagrams) {

        this();
        buildGrid(diagramDirectoryService, project, diagrams);
    }

    private void buildGrid(DiagramDirectoryService diagramDirectoryService, Project project, Set<DiagramDirectory> diagramDirectories, Set<Diagram> diagrams) {

        diagramDirectories
            .stream()
            .sorted(Comparator.comparing(DiagramDirectory::getCreatedAt))
            .forEach(diagramDirectory -> {
                CardLinkWrapper cardLinkWrapper = new CardLinkWrapper(diagramDirectoryService, project, diagramDirectory);
                add(cardLinkWrapper);
            });

        diagrams
            .stream()
            .sorted(Comparator.comparing(Diagram::getCreatedAt))
            .forEach(diagram -> {
                CardLinkWrapper cardLinkWrapper = new CardLinkWrapper(diagramDirectoryService, project, diagram);
                add(cardLinkWrapper);
            });
    }

    private void buildGrid(DiagramDirectoryService diagramDirectoryService, Project project, Set<Diagram> diagrams) {
        diagrams
            .stream()
            .sorted(Comparator.comparing(Diagram::getCreatedAt))
            .forEach(diagram -> {
                CardLinkWrapper cardLinkWrapper = new CardLinkWrapper(diagramDirectoryService, project, diagram);
                add(cardLinkWrapper);
            });
    }
}
