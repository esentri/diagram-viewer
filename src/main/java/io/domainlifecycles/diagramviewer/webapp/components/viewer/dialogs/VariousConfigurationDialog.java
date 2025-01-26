package io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class VariousConfigurationDialog extends Dialog {

    public VariousConfigurationDialog() {
        setHeaderTitle("Various configuration");

        VerticalLayout dialogLayout = createDialogLayout();
        add(dialogLayout);

        Button saveButton = new Button("Save", e -> close());
        Button cancelButton = new Button("Cancel", e -> close());
        getFooter().add(cancelButton);
        getFooter().add(saveButton);
    }

    private VerticalLayout createDialogLayout() {
        VerticalLayout dialogLayout = new VerticalLayout();

        dialogLayout.add(new TextField("Font"));
        dialogLayout.add(new TextField("Direction"));
        dialogLayout.add(new TextField("Ranker"));
        dialogLayout.add(new TextField("Acycler"));


        return dialogLayout;
    }
}
