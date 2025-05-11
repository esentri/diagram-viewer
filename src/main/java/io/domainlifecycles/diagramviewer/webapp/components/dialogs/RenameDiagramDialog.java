package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.RouteParameters;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramView;
import io.domainlifecycles.diagramviewer.webapp.views.ProjectView;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class RenameDiagramDialog extends Dialog {

    private final DiagramService diagramService;
    private final Project project;
    private final Binder<RenameDiagramOptions> binder;
    private final RenameDiagramOptions createDiagramOptions;
    private final Diagram diagram;

    private Button createButton;

    public RenameDiagramDialog(DiagramService diagramService, Project project, Diagram diagram) {
        this.diagramService = diagramService;
        this.project = project;
        this.diagram = diagram;
        this.binder = new Binder<>();

        this.createDiagramOptions = new RenameDiagramOptions(
            diagram.getFileName().replaceAll(diagram.getFileType().getFileSuffix(), ""));

        setHeaderTitle("Rename Diagram");

        getFooter().add(createSaveButton());
        getFooter().add(createCancelButton());
        add(createDialogLayout());

        binder.readBean(createDiagramOptions);
        binder.addStatusChangeListener(event -> createButton.setEnabled(binder.isValid()));
    }

    private Button createSaveButton() {
        createButton = new Button("Save");

        createButton.addClickListener(e -> {
            binder.writeBeanIfValid(createDiagramOptions);
            diagramService.update(diagram, project, createDiagramOptions.getFileName());
            close();
            UI.getCurrent().navigate(DiagramView.class, new RouteParameters(
                    Map.of(ProjectView.PROJECT_NAME_ROUTE_PARAMETER, project.getName(),
                        DiagramView.DIAGRAM_NAME_ROUTE_PARAMETER, diagram.getFileName())));
            ComponentUtil.fireEvent(UI.getCurrent(), new DiagramsOrProjectsChangedEvent(this, false));
        });

        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return createButton;
    }

    private Button createCancelButton() {
        return new Button("Cancel", e -> close());
    }

    private FormLayout createDialogLayout() {
        FormLayout formLayout = new FormLayout();

        TextField diagramNameTextField = new TextField();
        binder.forField(diagramNameTextField)
            .asRequired("Name is required.")
            .bind(RenameDiagramOptions::getFileName, RenameDiagramOptions::setFileName);
        formLayout.addFormItem(diagramNameTextField, "File-Name");

        return formLayout;
    }

    @Data
    @AllArgsConstructor
    private static class RenameDiagramOptions {
        private String fileName;
    }
}
