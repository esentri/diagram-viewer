package io.domainlifecycles.diagramviewer.webapp.components.viewer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.BuildingBlockStylesConfigurationDialog;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.ShowBuildingBlocksConfigurationDialog;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.ShowFieldsConfigurationDialog;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.VariousConfigurationDialog;
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
        List<Component> buildingBlockStylesButtonAndDialogButtonAndDialog = getBuildingBlockStylesButtonAndDialog();
        List<Component> showBuildingBlocksButtonAndDialog = getShowBuildingBlocksButtonAndDialog();
        List<Component> showFieldsButtonAndDialog = getShowFieldsButtonAndDialog();
        List<Component> variousConfigurationDialog = getVariousConfigurationDialog();

        return Stream.of(
                buildingBlockStylesButtonAndDialogButtonAndDialog,
                showBuildingBlocksButtonAndDialog,
                showFieldsButtonAndDialog,
                variousConfigurationDialog)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    private List<Component> getBuildingBlockStylesButtonAndDialog() {
        Dialog colorConfigurationDialog = new BuildingBlockStylesConfigurationDialog();
        return List.of(new Button(new Icon("vaadin:paintbrush"), e -> colorConfigurationDialog.open()), colorConfigurationDialog);
    }

    private List<Component> getShowBuildingBlocksButtonAndDialog() {
        Dialog showBuildingBlocksConfigurationDialog = new ShowBuildingBlocksConfigurationDialog();
        return List.of(new Button(new Icon("vaadin:connect"), e -> showBuildingBlocksConfigurationDialog.open()), showBuildingBlocksConfigurationDialog);
    }

    private List<Component> getShowFieldsButtonAndDialog() {
        Dialog showFieldsConfigurationDialog = new ShowFieldsConfigurationDialog();
        return List.of(new Button(new Icon("vaadin:input"), e -> showFieldsConfigurationDialog.open()), showFieldsConfigurationDialog);
    }

    private List<Component> getVariousConfigurationDialog() {
        VariousConfigurationDialog variousConfigurationDialog = new VariousConfigurationDialog();
        return List.of(new Button(new Icon("vaadin:font"), e -> variousConfigurationDialog.open()), variousConfigurationDialog);
    }
}
