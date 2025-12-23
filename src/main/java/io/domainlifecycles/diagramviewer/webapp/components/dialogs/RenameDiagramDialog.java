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
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramView;
import io.domainlifecycles.diagramviewer.webapp.views.ProjectView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class RenameDiagramDialog extends Dialog {

    private final DiagramService diagramService;
    private final Binder<RenameDiagramOptions> binder;
    @Setter
    private Diagram diagram;

    private RenameDiagramOptions renameDiagramOptions;
    private Button createButton;


    public RenameDiagramDialog(DiagramService diagramService) {
        this.diagramService = diagramService;
        this.binder = new Binder<>();

        setHeaderTitle("Rename Diagram");

        getFooter().add(createSaveButton());
        getFooter().add(createCancelButton());
        add(createDialogLayout());

        addOpenedChangeListener(e -> {
            if(e.isOpened()) {
                this.renameDiagramOptions = new RenameDiagramOptions(diagram.getName());
                binder.readBean(renameDiagramOptions);
            }
        });

        binder.addStatusChangeListener(event -> createButton.setEnabled(binder.isValid()));
    }

    private Button createSaveButton() {
        createButton = new Button("Save");

        createButton.addClickListener(e -> {
            binder.writeBeanIfValid(renameDiagramOptions);
            diagramService.rename(diagram, renameDiagramOptions.getFileName());
            close();
            UI.getCurrent().navigate(DiagramView.class, new RouteParameters(
                    Map.of(ProjectView.PROJECT_NAME_ROUTE_PARAMETER, diagram.getProject().getName(),
                        DiagramView.DIAGRAM_NAME_ROUTE_PARAMETER, diagram.getName())));
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
        formLayout.addFormItem(diagramNameTextField, "Name");

        return formLayout;
    }

    @Data
    @AllArgsConstructor
    private static class RenameDiagramOptions {
        private String fileName;
    }
}
