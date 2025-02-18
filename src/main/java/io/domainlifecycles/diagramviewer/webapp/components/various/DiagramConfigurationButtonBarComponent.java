package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import io.domainlifecycles.diagramviewer.session.AnalyzedDomainModel;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.StylingConfigurationDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.VariousConfigurationDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.VisibilityConfigurationDialog;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.DomainModelDialog;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiagramConfigurationButtonBarComponent extends FlexLayout {

    public DiagramConfigurationButtonBarComponent(AnalyzedDomainModel analyzedDomainModel) {
        setJustifyContentMode(JustifyContentMode.CENTER);
        setFlexDirection(FlexDirection.COLUMN);
        add(createAndGetConfigurationButtonsAndDialogs(analyzedDomainModel));
    }

    private List<Component> createAndGetConfigurationButtonsAndDialogs(AnalyzedDomainModel analyzedDomainModel) {
        List<Component> domainModelButtonAndDialog = getDomainModelButtonAndDialog(analyzedDomainModel);
        List<Component> stylingConfigurationButtonAndDialog = getStylingConfigurationButtonAndDialog(analyzedDomainModel);
        List<Component> visibilityConfigurationButtonAndDialog = getVisibilityConfigurationButtonAndDialog(analyzedDomainModel);
        List<Component> variousConfigurationButtonAndDialog = getVariousConfigurationButtonAndDialog(analyzedDomainModel);
        return Stream.of(
                domainModelButtonAndDialog,
                stylingConfigurationButtonAndDialog,
                visibilityConfigurationButtonAndDialog,
                variousConfigurationButtonAndDialog)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }



    private List<Component> getDomainModelButtonAndDialog(AnalyzedDomainModel analyzedDomainModel) {
        Dialog domainModelDialog = new DomainModelDialog(analyzedDomainModel);
        return List.of(new Button(new Icon("vaadin:file-tree-small"), e -> domainModelDialog.open()), domainModelDialog);
    }

    private List<Component> getStylingConfigurationButtonAndDialog(AnalyzedDomainModel analyzedDomainModel) {
        Dialog stylingConfigurationDialog = new StylingConfigurationDialog(analyzedDomainModel);
        return List.of(new Button(new Icon("vaadin:paintbrush"), e -> stylingConfigurationDialog.open()), stylingConfigurationDialog);
    }

    private List<Component> getVisibilityConfigurationButtonAndDialog(AnalyzedDomainModel analyzedDomainModel) {
        Dialog visibilityConfigurationDialog = new VisibilityConfigurationDialog(analyzedDomainModel);
        return List.of(new Button(new Icon("vaadin:eye"), e -> visibilityConfigurationDialog.open()), visibilityConfigurationDialog);
    }

    private List<Component> getVariousConfigurationButtonAndDialog(AnalyzedDomainModel analyzedDomainModel) {
        Dialog variousConfigurationDialog = new VariousConfigurationDialog(analyzedDomainModel);
        return List.of(new Button(new Icon("vaadin:cogs"), e -> variousConfigurationDialog.open()), variousConfigurationDialog);
    }
}
