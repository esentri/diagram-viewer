package io.domainlifecycles.diagramviewer.webapp.components.viewer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.ColorConfigurationDialog;
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
        List<Component> colorButtonAndDialog = getColorButtonAndDialog();
        List<Component> fontButtonAndDialog = getShowFontButtonAndDialog();
        List<Component> showFieldsButtonAndDialog = getShowFieldsButtonAndDialog();
        List<Component> showBuildingBlocksButtonAndDialog = getShowFontButtonAndDialog();

        return Stream.of(
                colorButtonAndDialog,
                fontButtonAndDialog,
                showFieldsButtonAndDialog,
                showBuildingBlocksButtonAndDialog)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    private List<Component> getShowFontButtonAndDialog() {
        return List.of(new Button(new Icon("vaadin:font")));
    }

    private List<Component> getShowFieldsButtonAndDialog() {
        return List.of(new Button(new Icon("vaadin:input")));
    }

    private List<Component> getShowBuildingBlocksButtonAndDialog() {
        return List.of(new Button(new Icon("vaadin:connect")));
    }

    private List<Component> getColorButtonAndDialog() {
        Dialog colorConfigurationDialog = new ColorConfigurationDialog();
        return List.of(new Button(new Icon("vaadin:paintbrush"), e -> colorConfigurationDialog.open()), colorConfigurationDialog);
    }
}
