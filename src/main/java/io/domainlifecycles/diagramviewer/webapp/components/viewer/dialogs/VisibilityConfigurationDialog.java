package io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class VisibilityConfigurationDialog extends Dialog {

    private Checkbox showAllFieldsCheckbox;
    private Checkbox showFullQualifiedClassNamesCheckbox;


    public VisibilityConfigurationDialog() {
        setHeaderTitle("Configuration | Visibility");

        createAndAddDialogLayout();
        generateAndAddFooter();
    }

    private void generateAndAddFooter() {
        Button saveButton = new Button("Save", e -> close());
        Button cancelButton = new Button("Cancel", e -> close());
        getFooter().add(cancelButton);
        getFooter().add(saveButton);
    }

    private VerticalLayout createCheckboxes() {
        VerticalLayout checkboxLayout = new VerticalLayout();

        showAllFieldsCheckbox = new Checkbox();

        checkboxLayout.add(showAllFieldsCheckbox);
        return checkboxLayout;
    }

    private void createAndAddDialogLayout() {
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.add(createCheckboxes());

        Accordion accordion = new Accordion();

        AccordionPanel aggregateRootAccordionPanel = new AccordionPanel("Aggregate Root");
        Input colorPicker = new Input();
        colorPicker.setType("color");
        aggregateRootAccordionPanel.add(colorPicker);
        accordion.add(aggregateRootAccordionPanel);

        add(dialogLayout);
    }
}
