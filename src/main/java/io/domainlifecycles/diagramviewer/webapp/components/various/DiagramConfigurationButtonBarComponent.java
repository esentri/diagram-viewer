package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.session.DomainModelSessionStorage;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.StylingConfigurationDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.VariousConfigurationDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.VisibilityConfigurationDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.DomainModelDialog;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiagramConfigurationButtonBarComponent extends FlexLayout {

    public DiagramConfigurationButtonBarComponent(Diagram diagram) {
        setJustifyContentMode(JustifyContentMode.CENTER);
        setFlexDirection(FlexDirection.COLUMN);
        add(createAndGetConfigurationButtonsAndDialogs(diagram));
    }

    private List<Component> createAndGetConfigurationButtonsAndDialogs(Diagram diagram) {
        List<Component> domainModelButtonAndDialog = getDomainModelButtonAndDialog(diagram);
        List<Component> stylingConfigurationButtonAndDialog = getStylingConfigurationButtonAndDialog(diagram);
        List<Component> visibilityConfigurationButtonAndDialog = getVisibilityConfigurationButtonAndDialog(diagram);
        List<Component> variousConfigurationButtonAndDialog = getVariousConfigurationButtonAndDialog(diagram);
        return Stream.of(
                domainModelButtonAndDialog,
                stylingConfigurationButtonAndDialog,
                visibilityConfigurationButtonAndDialog,
                variousConfigurationButtonAndDialog)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }



    private List<Component> getDomainModelButtonAndDialog(Diagram diagram) {
        Dialog domainModelDialog = new DomainModelDialog(diagram);
        return List.of(new Button(new Icon("vaadin:file-tree-small"), e -> domainModelDialog.open()), domainModelDialog);
    }

    private List<Component> getStylingConfigurationButtonAndDialog(Diagram diagram) {
        Dialog stylingConfigurationDialog = new StylingConfigurationDialog(diagram);
        return List.of(new Button(new Icon("vaadin:paintbrush"), e -> stylingConfigurationDialog.open()), stylingConfigurationDialog);
    }

    private List<Component> getVisibilityConfigurationButtonAndDialog(Diagram diagram) {
        Dialog visibilityConfigurationDialog = new VisibilityConfigurationDialog(diagram);
        return List.of(new Button(new Icon("vaadin:eye"), e -> visibilityConfigurationDialog.open()), visibilityConfigurationDialog);
    }

    private List<Component> getVariousConfigurationButtonAndDialog(Diagram diagram) {
        Dialog variousConfigurationDialog = new VariousConfigurationDialog(diagram);
        return List.of(new Button(new Icon("vaadin:cogs"), e -> variousConfigurationDialog.open()), variousConfigurationDialog);
    }
}
