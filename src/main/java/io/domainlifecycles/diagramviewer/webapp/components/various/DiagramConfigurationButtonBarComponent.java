package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.StylingConfigurationDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.VariousConfigurationDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.VisibilityConfigurationDialog;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiagramConfigurationButtonBarComponent extends FlexLayout {

    public DiagramConfigurationButtonBarComponent(Project project, Diagram diagram, DiagramService diagramService) {
        setJustifyContentMode(JustifyContentMode.CENTER);
        setFlexDirection(FlexDirection.COLUMN);
        add(createAndGetConfigurationButtonsAndDialogs(project, diagram, diagramService));
    }

    private List<Component> createAndGetConfigurationButtonsAndDialogs(Project project, Diagram diagram, DiagramService diagramService) {
        List<Component> stylingConfigurationButtonAndDialog = getStylingConfigurationButtonAndDialog(project, diagram, diagramService);
        List<Component> visibilityConfigurationButtonAndDialog = getVisibilityConfigurationButtonAndDialog(project, diagram, diagramService);
        List<Component> variousConfigurationButtonAndDialog = getVariousConfigurationButtonAndDialog(project, diagram, diagramService);
        return Stream.of(
                stylingConfigurationButtonAndDialog,
                visibilityConfigurationButtonAndDialog,
                variousConfigurationButtonAndDialog)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    private List<Component> getStylingConfigurationButtonAndDialog(Project project, Diagram diagram, DiagramService diagramService) {
        Dialog stylingConfigurationDialog = new StylingConfigurationDialog(project, diagram, diagramService);
        return List.of(new Button(new Icon("vaadin:paintbrush"), e -> stylingConfigurationDialog.open()), stylingConfigurationDialog);
    }

    private List<Component> getVisibilityConfigurationButtonAndDialog(Project project, Diagram diagram, DiagramService diagramService) {
        Dialog visibilityConfigurationDialog = new VisibilityConfigurationDialog(diagramService, project, diagram);
        return List.of(new Button(new Icon("vaadin:eye"), e -> visibilityConfigurationDialog.open()), visibilityConfigurationDialog);
    }

    private List<Component> getVariousConfigurationButtonAndDialog(Project project, Diagram diagram, DiagramService diagramService) {
        Dialog variousConfigurationDialog = new VariousConfigurationDialog(diagram, project, diagramService);
        return List.of(new Button(new Icon("vaadin:cogs"), e -> variousConfigurationDialog.open()), variousConfigurationDialog);
    }
}
