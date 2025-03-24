package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.StylingConfigurationDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.VariousConfigurationDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.VisibilityConfigurationDialog;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiagramConfigurationButtonBarComponent extends FlexLayout {

    public DiagramConfigurationButtonBarComponent(Diagram diagram, DiagramService diagramService, RefreshCallback callback) {
        setJustifyContentMode(JustifyContentMode.CENTER);
        setFlexDirection(FlexDirection.COLUMN);
        add(createAndGetConfigurationButtonsAndDialogs(diagram, diagramService, callback));
    }

    private List<Component> createAndGetConfigurationButtonsAndDialogs(Diagram diagram, DiagramService diagramService, RefreshCallback callback) {
        List<Component> stylingConfigurationButtonAndDialog = getStylingConfigurationButtonAndDialog(diagram, diagramService, callback);
        List<Component> visibilityConfigurationButtonAndDialog = getVisibilityConfigurationButtonAndDialog(diagram, diagramService, callback);
        List<Component> variousConfigurationButtonAndDialog = getVariousConfigurationButtonAndDialog(diagram, diagramService, callback);
        return Stream.of(
                stylingConfigurationButtonAndDialog,
                visibilityConfigurationButtonAndDialog,
                variousConfigurationButtonAndDialog)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    private List<Component> getStylingConfigurationButtonAndDialog(Diagram diagram, DiagramService diagramService, RefreshCallback callback) {
        Dialog stylingConfigurationDialog = new StylingConfigurationDialog(diagram, diagramService, callback);
        return List.of(new Button(new Icon("vaadin:paintbrush"), e -> stylingConfigurationDialog.open()), stylingConfigurationDialog);
    }

    private List<Component> getVisibilityConfigurationButtonAndDialog(Diagram diagram, DiagramService diagramService, RefreshCallback callback) {
        Dialog visibilityConfigurationDialog = new VisibilityConfigurationDialog(diagram, diagramService, callback);
        return List.of(new Button(new Icon("vaadin:eye"), e -> visibilityConfigurationDialog.open()), visibilityConfigurationDialog);
    }

    private List<Component> getVariousConfigurationButtonAndDialog(Diagram diagram, DiagramService diagramService, RefreshCallback callback) {
        Dialog variousConfigurationDialog = new VariousConfigurationDialog(diagram, diagramService, callback);
        return List.of(new Button(new Icon("vaadin:cogs"), e -> variousConfigurationDialog.open()), variousConfigurationDialog);
    }
}
