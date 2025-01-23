package io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.button.Button;
import java.util.List;

/**
 * Dialog allowing configuration for each DDD building block (i.e. AggregateRoot, Repository, etc.).
 *
 * @author leonvoellinger
 */
public class BuildingBlockStylesConfigurationDialog extends Dialog {

    public BuildingBlockStylesConfigurationDialog() {
        setHeaderTitle("Configuration | DDD block styling");

        VerticalLayout dialogLayout = createDialogLayout();
        add(dialogLayout);

        Button saveButton = new Button("Save", e -> close());
        Button cancelButton = new Button("Cancel", e -> close());
        getFooter().add(cancelButton);
        getFooter().add(saveButton);
    }

    private VerticalLayout createDialogLayout() {
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSizeFull();
        Accordion accordion = new Accordion();
        accordion.setSizeFull();

        TextField color = new TextField("Color");
        color.setWidthFull();
        accordion.add("Aggregate Root", new VerticalLayout(color, new Checkbox("Bold")));
        accordion.add("Entity", new VerticalLayout(new TextField("Color"), new Checkbox("Bold")));
        accordion.add("Value Object", new VerticalLayout(new TextField("Color"), new Checkbox("Bold")));
        accordion.add("Enum", new VerticalLayout(new TextField("Color"), new Checkbox("Bold")));

        dialogLayout.add(accordion);
        return dialogLayout;
    }
}
