package io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;

public class VariousConfigurationDialog extends Dialog {

    private static final String[] FONT_SELECT_VALUES = {"Helvetica", "Arial"};
    private static final String[] DIRECTION_SELECT_VALUES = {"Up", "Down"};
    private static final String[] RANKER_SELECT_VALUES = {"longest-path", "network-simplex", "tight-tree"};
    private static final String[] ACYCLER_SELECT_VALUES = {"greedy"};

    private Button cancelButton;
    private Button saveButton;
    private Select<String> fontSelect;
    private Select<String> directionSelect;
    private Select<String> rankerSelect;
    private Select<String> acyclerSelect;

    public VariousConfigurationDialog() {
        setHeaderTitle("Configuration | Various");

        add(createDialogLayout());

        getFooter().add(createSaveButton());
        getFooter().add(createCancelButton());
    }

    private Button createSaveButton() {
        saveButton = new Button("Save");

        saveButton.addClickListener(e -> {
            // execute DLC logic
            close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return saveButton;
    }

    private Button createCancelButton() {
        cancelButton = new Button("Cancel", e -> close());
        return cancelButton;
    }

    private FormLayout createDialogLayout() {
        FormLayout formLayout = new FormLayout();

        fontSelect = new Select<>();
        fontSelect.setItems(FONT_SELECT_VALUES);
        fontSelect.setValue(FONT_SELECT_VALUES[0]);
        formLayout.addFormItem(fontSelect,"Font");

        directionSelect = new Select<>();
        directionSelect.setItems(DIRECTION_SELECT_VALUES);
        directionSelect.setValue(DIRECTION_SELECT_VALUES[0]);
        formLayout.addFormItem(directionSelect,"Direction");

        rankerSelect = new Select<>();
        rankerSelect.setItems(RANKER_SELECT_VALUES);
        rankerSelect.setValue(RANKER_SELECT_VALUES[0]);
        formLayout.addFormItem(rankerSelect,"Ranker");

        acyclerSelect = new Select<>();
        acyclerSelect.setItems(ACYCLER_SELECT_VALUES);
        acyclerSelect.setValue(ACYCLER_SELECT_VALUES[0]);
        formLayout.addFormItem(acyclerSelect,"Acycler");

        return formLayout;
    }
}
