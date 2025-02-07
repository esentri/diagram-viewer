package io.domainlifecycles.diagramviewer.webapp.components.viewer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.data.binder.Binder;
import io.domainlifecycles.diagramviewer.generate.SQLDDLGeneratorService;
import io.domainlifecycles.diagramviewer.model.DiagramConfiguration;
import io.domainlifecycles.diagramviewer.session.AnalyzedDomainModel;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.DomainModelDialog;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.GenerateDatabaseModelDialog;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.StylingConfigurationDialog;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.VariousConfigurationDialog;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.VisibilityConfigurationDialog;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiagramConfigurationButtonBarComponent extends FlexLayout {

    public DiagramConfigurationButtonBarComponent(
            SQLDDLGeneratorService sqlddlGeneratorService,
            AnalyzedDomainModel analyzedDomainModel
    ) {
        setJustifyContentMode(JustifyContentMode.CENTER);
        setFlexDirection(FlexDirection.COLUMN);
        add(createAndGetConfigurationButtonsAndDialogs(sqlddlGeneratorService, analyzedDomainModel));
    }

    private List<Component> createAndGetConfigurationButtonsAndDialogs(
            SQLDDLGeneratorService sqlddlGeneratorService,
            AnalyzedDomainModel analyzedDomainModel
    ) {
        List<Component> domainModelButtonAndDialog = getDomainModelButtonAndDialog(analyzedDomainModel);
        List<Component> stylingConfigurationButtonAndDialog = getStylingConfigurationButtonAndDialog(analyzedDomainModel);
        List<Component> visibilityConfigurationButtonAndDialog = getVisibilityConfigurationButtonAndDialog(analyzedDomainModel);
        List<Component> variousConfigurationButtonAndDialog = getVariousConfigurationButtonAndDialog(analyzedDomainModel);
        List<Component> generateDataBaseModelButtonAndDialog = getGenerateDataBaseModelButtonAndDialog(sqlddlGeneratorService, analyzedDomainModel);
        return Stream.of(
                domainModelButtonAndDialog,
                stylingConfigurationButtonAndDialog,
                visibilityConfigurationButtonAndDialog,
                variousConfigurationButtonAndDialog,
                generateDataBaseModelButtonAndDialog)
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

    private List<Component> getGenerateDataBaseModelButtonAndDialog(SQLDDLGeneratorService sqlddlGeneratorService, AnalyzedDomainModel analyzedDomainModel) {
        Dialog generateDatabaseModelDialog = new GenerateDatabaseModelDialog(sqlddlGeneratorService, analyzedDomainModel);
        return List.of(new Button(new Icon("vaadin:database"), e -> generateDatabaseModelDialog.open()), generateDatabaseModelDialog);
    }
}
