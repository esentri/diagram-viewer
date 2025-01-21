package io.domainlifecycles.diagramviewer.webapp.components.viewer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import java.util.List;

public class DiagramConfigurationButtonBarComponent extends FlexLayout {

    public DiagramConfigurationButtonBarComponent() {
        setJustifyContentMode(JustifyContentMode.CENTER);
        setFlexDirection(FlexDirection.COLUMN);

        add(getConfigurationButtons());
    }

    private List<Component> getConfigurationButtons() {
        Button colorButton = new Button(new Icon("vaadin:paintbrush"));
        Button fontButton = new Button(new Icon("vaadin:font"));
        Button showFieldsButton = new Button(new Icon("vaadin:input"));
        Button showBuildingBlocksButton = new Button(new Icon("vaadin:connect-o"));

        return List.of(colorButton, fontButton, showFieldsButton, showBuildingBlocksButton);
    }
}
