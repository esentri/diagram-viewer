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
import io.domainlifecycles.diagramviewer.model.DiagramDirectory;
import io.domainlifecycles.diagramviewer.service.DiagramDirectoryService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramDirectoryView;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramView;
import io.domainlifecycles.diagramviewer.webapp.views.ProjectView;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

public class RenameDiagramDirectoryDialog extends Dialog {

    private final DiagramDirectoryService diagramDirectoryService;
    private final DiagramDirectory diagramDirectory;
    private final Binder<RenameDiagramDirectoryOptions> binder;
    private final RenameDiagramDirectoryOptions renameDiagramDirectoryOptions;

    private Button createButton;

    public RenameDiagramDirectoryDialog(DiagramDirectoryService diagramDirectoryService, DiagramDirectory diagramDirectory) {
        this.diagramDirectoryService = diagramDirectoryService;
        this.diagramDirectory = diagramDirectory;
        this.binder = new Binder<>();

        this.renameDiagramDirectoryOptions = new RenameDiagramDirectoryOptions(diagramDirectory.getName());

        setHeaderTitle("Rename Directory");

        getFooter().add(createSaveButton());
        getFooter().add(createCancelButton());
        add(createDialogLayout());

        binder.readBean(renameDiagramDirectoryOptions);
        binder.addStatusChangeListener(event -> createButton.setEnabled(binder.isValid()));
    }

    private Button createSaveButton() {
        createButton = new Button("Save");

        createButton.addClickListener(e -> {
            binder.writeBeanIfValid(renameDiagramDirectoryOptions);
            diagramDirectoryService.update(diagramDirectory, renameDiagramDirectoryOptions.getName());
            close();
            UI.getCurrent().navigate(DiagramDirectoryView.class, new RouteParameters(
                    Map.of(DiagramDirectoryView.DIAGRAM_DIRECTORY_NAME_ROUTE_PARAMETER, diagramDirectory.getName())));
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

        TextField diagramDirectoryNameTextField = new TextField();
        binder.forField(diagramDirectoryNameTextField)
            .asRequired("Name is required.")
            .bind(RenameDiagramDirectoryOptions::getName, RenameDiagramDirectoryOptions::setName);
        formLayout.addFormItem(diagramDirectoryNameTextField, "Name");

        return formLayout;
    }

    @Data
    @AllArgsConstructor
    private static class RenameDiagramDirectoryOptions {
        private String name;
    }
}
