package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.StylingConfigurationDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.VariousConfigurationDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.VisibilityConfigurationDialog;

import java.util.List;

public class DiagramConfigurationButtonBarComponent extends FlexLayout {

    private final StylingConfigurationDialog stylingConfigurationDialog;
    private final VisibilityConfigurationDialog visibilityConfigurationDialog;
    private final VariousConfigurationDialog variousConfigurationDialog;

    public DiagramConfigurationButtonBarComponent(DiagramService diagramService) {

        this.stylingConfigurationDialog = new StylingConfigurationDialog(diagramService);
        add(stylingConfigurationDialog);
        this. visibilityConfigurationDialog = new VisibilityConfigurationDialog(diagramService);
        add(visibilityConfigurationDialog);
        this.variousConfigurationDialog = new VariousConfigurationDialog(diagramService);
        add(variousConfigurationDialog);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setFlexDirection(FlexDirection.COLUMN);
        add(createAndGetConfigurationButtons());
    }

    public void setDiagram(Diagram diagram){
        stylingConfigurationDialog.setDiagram(diagram);
        variousConfigurationDialog.setDiagram(diagram);
        visibilityConfigurationDialog.setDiagram(diagram);
    }

    private List<Component> createAndGetConfigurationButtons() {
        List<Component> buttons = List.of(getStylingConfigurationButton(), getVisibilityConfigurationButton(), getVariousConfigurationButton());
        return buttons;
    }

    private Button getStylingConfigurationButton() {
        Button stylingConfigurationButton = new Button(new Icon(VaadinIcon.PAINTBRUSH), e -> {
            stylingConfigurationDialog.open();
        });
        stylingConfigurationButton.getStyle().set("cursor", "pointer");
        return stylingConfigurationButton;
    }

    private Button getVisibilityConfigurationButton() {
        Button visibilityConfigurationButton = new Button(new Icon(VaadinIcon.EYE), e -> {
            visibilityConfigurationDialog.open();
        });
        visibilityConfigurationButton.getStyle().set("cursor", "pointer");
        return visibilityConfigurationButton;
    }

    private Button getVariousConfigurationButton() {
        Button variousConfigurationButton = new Button(new Icon(VaadinIcon.COGS), e -> {
            variousConfigurationDialog.open();
        });
        variousConfigurationButton.getStyle().set("cursor", "pointer");
        return variousConfigurationButton;
    }
}
