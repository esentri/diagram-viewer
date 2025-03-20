package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.binder.Binder;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.Acycler;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.Direction;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.Ranker;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.Font;

public class VariousConfigurationDialog extends Dialog {

    private final Diagram diagram;
    private final Binder<DiagramStylingConfiguration> diagramConfigurationBinder;

    private Button cancelButton;
    private Button saveButton;
    private Select<Font> fontSelect;
    private Select<Direction> directionSelect;
    private Select<Ranker> rankerSelect;
    private Select<Acycler> acyclerSelect;

    public VariousConfigurationDialog(Diagram diagram) {
        this.diagramConfigurationBinder = new Binder<>(DiagramStylingConfiguration.class);
        this.diagram = diagram;

        setHeaderTitle("Configuration | Various");

        add(createDialogLayout());
        diagramConfigurationBinder.readBean(diagram.getDiagramStylingConfiguration());

        getFooter().add(createSaveButton());
        getFooter().add(createCancelButton());
    }

    private Button createSaveButton() {
        saveButton = new Button("Save");

        saveButton.addClickListener(e -> {
            diagramConfigurationBinder.writeBeanIfValid(diagram.getDiagramStylingConfiguration());
            close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return saveButton;
    }

    private Button createCancelButton() {
        cancelButton = new Button("Cancel", e -> {
            diagramConfigurationBinder.readBean(diagram.getDiagramStylingConfiguration());
            close();
        });
        return cancelButton;
    }

    private FormLayout createDialogLayout() {
        FormLayout formLayout = new FormLayout();

        fontSelect = new Select<>();
        fontSelect.setItems(Font.values());
        fontSelect.setItemLabelGenerator(Font::getDisplayValue);
        formLayout.addFormItem(fontSelect,"Font");
        diagramConfigurationBinder.forField(fontSelect).bind(DiagramStylingConfiguration::getFont, DiagramStylingConfiguration::setFont);

        directionSelect = new Select<>();
        directionSelect.setItems(Direction.values());
        directionSelect.setItemLabelGenerator(Direction::getDisplayValue);
        formLayout.addFormItem(directionSelect,"Direction");
        diagramConfigurationBinder.forField(directionSelect).bind(DiagramStylingConfiguration::getDirection, DiagramStylingConfiguration::setDirection);

        rankerSelect = new Select<>();
        rankerSelect.setItems(Ranker.values());
        rankerSelect.setItemLabelGenerator(Ranker::getDisplayValue);
        formLayout.addFormItem(rankerSelect,"Ranker");
        diagramConfigurationBinder.forField(rankerSelect).bind(DiagramStylingConfiguration::getRanker, DiagramStylingConfiguration::setRanker);

        acyclerSelect = new Select<>();
        acyclerSelect.setItems(Acycler.values());
        acyclerSelect.setItemLabelGenerator(Acycler::getDisplayValue);
        formLayout.addFormItem(acyclerSelect,"Acycler");
        diagramConfigurationBinder.forField(acyclerSelect).bind(DiagramStylingConfiguration::getAcycler, DiagramStylingConfiguration::setAcycler);

        return formLayout;
    }
}
