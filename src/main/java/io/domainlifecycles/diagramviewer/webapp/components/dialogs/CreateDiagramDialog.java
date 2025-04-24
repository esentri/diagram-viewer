package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import io.domainlifecycles.diagramviewer.kroki.FileType;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;

public class CreateDiagramDialog extends Dialog {

    private final DiagramService diagramService;
    private final Project project;
    private String fileName;
    private String packageName;
    private FileType fileType;

    public CreateDiagramDialog(DiagramService diagramService, Project project) {
        this.diagramService = diagramService;
        this.project = project;

        setHeaderTitle("Create Diagram");

        add(createDialogLayout());
        getFooter().add(createCreateButton());
        getFooter().add(createCancelButton());
    }

    private Button createCreateButton() {
        Button createButton = new Button("Create");

        createButton.addClickListener(e -> {
            diagramService.create(project, fileName, packageName, fileType);
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
        diagramNameTextField.addValueChangeListener(e -> fileName = e.getValue());
        formLayout.addFormItem(diagramNameTextField, "File-Name");

        TextField packageNameTextField = new TextField();
        packageNameTextField.addValueChangeListener(e -> packageName = e.getValue());
        formLayout.addFormItem(packageNameTextField, "Package-Name");

        Select<FileType> formatSelect = new Select<>();
        formatSelect.setItems(FileType.values());
        formatSelect.setItemLabelGenerator(FileType::getDisplayValue);
        formatSelect.addValueChangeListener(e -> fileType = e.getValue());
        formLayout.addFormItem(formatSelect,"Format");

        return formLayout;
    }
}
