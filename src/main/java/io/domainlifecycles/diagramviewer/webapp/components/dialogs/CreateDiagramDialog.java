package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import io.domainlifecycles.diagramviewer.kroki.FileType;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.mirror.api.BoundedContextMirror;
import java.util.Set;
import java.util.stream.Collectors;

public class CreateDiagramDialog extends Dialog {

    private final DiagramService diagramService;
    private final Project project;
    private String fileName;
    private FileType fileType;
    private String selectedContextPackage;

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
            diagramService.create(project, fileName, selectedContextPackage, fileType);
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

        Select<String> boundedContextPackageSelect = new Select<>();
        boundedContextPackageSelect.setItems(mapPackageNames());
        boundedContextPackageSelect.addValueChangeListener(e -> selectedContextPackage = e.getValue());
        formLayout.addFormItem(boundedContextPackageSelect, "Context-Package");

        Select<FileType> formatSelect = new Select<>();
        formatSelect.setItems(FileType.values());
        formatSelect.setItemEnabledProvider(item -> item.equals(FileType.SVG));
        formatSelect.addValueChangeListener(e -> fileType = e.getValue());
        formLayout.addFormItem(formatSelect, "Format");

        return formLayout;
    }

    private Set<String> mapPackageNames() {
        return project.getDomainModel().boundedContextMirrors().stream().map(
            BoundedContextMirror::getPackageName).collect(
            Collectors.toSet());
    }
}
