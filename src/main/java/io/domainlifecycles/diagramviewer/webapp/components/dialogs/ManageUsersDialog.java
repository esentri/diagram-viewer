package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import io.domainlifecycles.diagramviewer.model.Project;

public class ManageUsersDialog extends Dialog {

    private final Project project;

    public ManageUsersDialog(Project project) {
        this.project = project;

        setHeaderTitle("Manage Users");

        add(createDialogLayout());
        getFooter().add(createCreateButton());
        getFooter().add(createCancelButton());
    }

    private Button createCreateButton() {
        Button applyButton = new Button("Apply");

        applyButton.addClickListener(e -> {
            close();
        });

        applyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return applyButton;
    }

    private Button createCancelButton() {
        return new Button("Cancel", e -> close());
    }

    private FormLayout createDialogLayout() {
        FormLayout formLayout = new FormLayout();

        return formLayout;
    }
}
