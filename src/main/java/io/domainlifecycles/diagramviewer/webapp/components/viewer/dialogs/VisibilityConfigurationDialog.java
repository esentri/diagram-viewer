package io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class VisibilityConfigurationDialog extends Dialog {

    private Checkbox showAllFieldsCheckbox;
    private Checkbox showFullQualifiedClassNamesCheckbox;


    public VisibilityConfigurationDialog() {
        generateCheckboxes();

        setHeaderTitle("Configuration | Visibility");

        VerticalLayout dialogLayout = createDialogLayout();
        add(dialogLayout);

        Button saveButton = new Button("Save", e -> close());
        Button cancelButton = new Button("Cancel", e -> close());
        getFooter().add(cancelButton);
        getFooter().add(saveButton);
    }

    private void generateCheckboxes() {
        showAllFieldsCheckbox = new Checkbox();
    }

    private VerticalLayout createDialogLayout() {
        VerticalLayout dialogLayout = new VerticalLayout();

        dialogLayout.add(new Checkbox("Show Fields"));
        dialogLayout.add(new TextField("Color 2"));

        return dialogLayout;
    }
}
