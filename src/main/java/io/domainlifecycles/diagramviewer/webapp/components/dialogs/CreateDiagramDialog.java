package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.rest.kroki.FileType;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.components.various.PackageSelectChipField;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CreateDiagramDialog extends Dialog {

    private final DiagramService diagramService;
    private final Project project;
    private final Binder<CreateDiagramOptions> binder;
    private final CreateDiagramOptions createDiagramOptions;

    private Button createButton;

    public CreateDiagramDialog(DiagramService diagramService, Project project) {
        this.diagramService = diagramService;
        this.project = project;
        this.binder = new Binder<>();

        this.createDiagramOptions = new CreateDiagramOptions();

        setHeaderTitle("Create Diagram");

        getFooter().add(createCreateButton());
        getFooter().add(createCancelButton());
        add(createDialogLayout());

        binder.readBean(createDiagramOptions);
        binder.addStatusChangeListener(event -> createButton.setEnabled(binder.isValid()));
    }

    private Button createCreateButton() {
        createButton = new Button("Create");

        createButton.addClickListener(e -> {
            binder.writeBeanIfValid(createDiagramOptions);
            diagramService.create(project, createDiagramOptions.getFileName(), createDiagramOptions.getFileType(), createDiagramOptions.getFilteredPackages());
            ComponentUtil.fireEvent(UI.getCurrent(), new DiagramsOrProjectsChangedEvent(this, false));
            close();
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
            .bind(CreateDiagramOptions::getFileName, CreateDiagramOptions::setFileName);
        formLayout.addFormItem(diagramNameTextField, "File-Name");

        PackageSelectChipField packageSelectChipField = new PackageSelectChipField();
        packageSelectChipField.setWidthFull();
        binder.forField(packageSelectChipField)
                .bind(CreateDiagramOptions::getFilteredPackages, CreateDiagramOptions::setFilteredPackages);
        formLayout.addFormItem(packageSelectChipField, "Filtered packages");

        Select<FileType> formatSelect = new Select<>();
        formatSelect.setItems(FileType.values());
        formatSelect.setItemEnabledProvider(item -> item.equals(FileType.SVG));
        binder.forField(formatSelect)
            .asRequired("Format is required.")
            .bind(CreateDiagramOptions::getFileType, CreateDiagramOptions::setFileType);
        formLayout.addFormItem(formatSelect, "Format");

        return formLayout;
    }

    @Data
    @NoArgsConstructor
    private static class CreateDiagramOptions {
        private String fileName;
        private FileType fileType;
        private Set<String> filteredPackages;
    }
}
