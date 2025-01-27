package io.domainlifecycles.diagramviewer.webapp.components.viewer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.StylingConfigurationDialog;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.VariousConfigurationDialog;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.VisibilityConfigurationDialog;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiagramConfigurationButtonBarComponent extends FlexLayout {

    public DiagramConfigurationButtonBarComponent() {
        setJustifyContentMode(JustifyContentMode.CENTER);
        setFlexDirection(FlexDirection.COLUMN);

        add(getConfigurationButtonsAndDialogs());
    }

    private List<Component> getConfigurationButtonsAndDialogs() {
        List<Component> stylingConfigurationButtonAndDialog = getStylingConfigurationButtonAndDialog();
        List<Component> visibilityConfigurationButtonAndDialog = getVisibilityConfigurationButtonAndDialog();
        List<Component> variousConfigurationButtonAndDialog = getVariousConfigurationButtonAndDialog();

        return Stream.of(
                stylingConfigurationButtonAndDialog,
                visibilityConfigurationButtonAndDialog,
                variousConfigurationButtonAndDialog)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    private List<Component> getStylingConfigurationButtonAndDialog() {
        Dialog stylingConfigurationDialog = new StylingConfigurationDialog();
        return List.of(new Button(new Icon("vaadin:paintbrush"), e -> stylingConfigurationDialog.open()), stylingConfigurationDialog);
    }

    private List<Component> getVisibilityConfigurationButtonAndDialog() {
        Dialog visibilityConfigurationDialog = new VisibilityConfigurationDialog();
        return List.of(new Button(new Icon("vaadin:eye"), e -> visibilityConfigurationDialog.open()), visibilityConfigurationDialog);
    }

    private List<Component> getVariousConfigurationButtonAndDialog() {
        Dialog variousConfigurationDialog = new VariousConfigurationDialog();
        return List.of(new Button(new Icon("vaadin:cogs"), e -> variousConfigurationDialog.open()), variousConfigurationDialog);
    }
}
