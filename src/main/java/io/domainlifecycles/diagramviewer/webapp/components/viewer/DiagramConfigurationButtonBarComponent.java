package io.domainlifecycles.diagramviewer.webapp.components.viewer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.BuildingBlockStylesConfigurationDialog;
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
        List<Component> buildingBlockStylesButtonAndDialogButtonAndDialog = getBuildingBlockStylesConfigurationButtonAndDialog();
        List<Component> showBuildingBlocksButtonAndDialog = getVariousConfigurationButtonAndDialog();
        List<Component> showFieldsButtonAndDialog = getVisibilityConfigurationButtonAndDialog();

        return Stream.of(
                buildingBlockStylesButtonAndDialogButtonAndDialog,
                showBuildingBlocksButtonAndDialog,
                showFieldsButtonAndDialog)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    private List<Component> getBuildingBlockStylesConfigurationButtonAndDialog() {
        Dialog buildingBlockStylesConfigurationDialog = new BuildingBlockStylesConfigurationDialog();
        return List.of(new Button(new Icon("vaadin:paintbrush"), e -> buildingBlockStylesConfigurationDialog.open()), buildingBlockStylesConfigurationDialog);
    }

    private List<Component> getVariousConfigurationButtonAndDialog() {
        Dialog variousConfigurationDialog = new VariousConfigurationDialog();
        return List.of(new Button(new Icon("vaadin:connect"), e -> variousConfigurationDialog.open()), variousConfigurationDialog);
    }

    private List<Component> getVisibilityConfigurationButtonAndDialog() {
        Dialog visibilityConfigurationDialog = new VisibilityConfigurationDialog();
        return List.of(new Button(new Icon("vaadin:input"), e -> visibilityConfigurationDialog.open()), visibilityConfigurationDialog);
    }
}
