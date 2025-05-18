package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.DiagramDirectoryService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CreateFolderDialog extends Dialog {

    private final Project project;
    private final Diagram dragDiagram;
    private final Diagram dropDiagram;
    private final DiagramDirectoryService diagramDirectoryService;
    private final Binder<CreateFolderOptions> binder;

    private CreateFolderOptions createFolderOptions;
    private Button createButton;

    public CreateFolderDialog(Project project, Diagram dragDiagram, Diagram dropDiagram, DiagramDirectoryService diagramDirectoryService) {
        this.project = project;
        this.dragDiagram = dragDiagram;
        this.dropDiagram = dropDiagram;
        this.diagramDirectoryService = diagramDirectoryService;
        this.binder = new Binder<>();

        this.createFolderOptions = new CreateFolderOptions();

        setHeaderTitle("Create Folder");

        getFooter().add(createCreateButton());
        getFooter().add(createCancelButton());
        add(createDialogLayout());

        addOpenedChangeListener(e -> {
            if(e.isOpened()) {
                this.createFolderOptions = new CreateFolderOptions();
                binder.readBean(createFolderOptions);
            }
        });

        binder.addStatusChangeListener(event -> createButton.setEnabled(binder.isValid()));
    }

    private Button createCreateButton() {
        createButton = new Button("Create");

        createButton.addClickListener(e -> {
            binder.writeBeanIfValid(createFolderOptions);
            diagramDirectoryService.create(createFolderOptions.getFolderName(), project, Set.of(dragDiagram, dropDiagram));
            close();
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

        TextField folderNameTextField = new TextField();
        binder.forField(folderNameTextField)
            .asRequired("Name is required.")
            .bind(CreateFolderOptions::getFolderName, CreateFolderOptions::setFolderName);
        formLayout.addFormItem(folderNameTextField, "Name");

        return formLayout;
    }

    @Data
    @NoArgsConstructor
    private static class CreateFolderOptions {
        private String folderName;
    }
}
