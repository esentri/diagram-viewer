package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.EmailField;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramStylingChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.events.ProjectUsersChangedEvent;

public class AddUserDialog extends Dialog {

    private final Project project;
    private final ProjectService projectService;

    private String emailAddressValue;

    public AddUserDialog(Project project, ProjectService projectService) {
        this.project = project;
        this.projectService = projectService;

        setHeaderTitle("Add User");

        setWidth("30%");
        setHeight("40%");

        add(createDialogLayout());

        getFooter().add(createAddButton());
        getFooter().add(createCancelButton());
    }


    private Button createAddButton() {
        Button saveButton = new Button("Add");

        saveButton.addClickListener(e -> {
            projectService.assignUser(project, emailAddressValue);
            ComponentUtil.fireEvent(UI.getCurrent(), new ProjectUsersChangedEvent(this, false));
            close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return saveButton;
    }

    private Button createCancelButton() {
        return new Button("Cancel", e -> close());
    }

    private FormLayout createDialogLayout() {
        FormLayout formLayout = new FormLayout();

        EmailField emailField = new EmailField();
        emailField.getElement().setAttribute("name", "email");
        emailField.setErrorMessage("Not a valid email address");
        emailField.setClearButtonVisible(true);
        emailField.addValueChangeListener(valueChanged -> emailAddressValue = valueChanged.getValue());

        formLayout.addFormItem(emailField, "Email address");

        return formLayout;
    }
}
