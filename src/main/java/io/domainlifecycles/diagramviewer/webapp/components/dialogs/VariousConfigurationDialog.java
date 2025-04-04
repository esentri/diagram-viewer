package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.binder.Binder;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.Acycler;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.Direction;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.Font;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.Ranker;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramStylingChangedEvent;

public class VariousConfigurationDialog extends Dialog {

    private final Diagram diagram;
    private final DiagramService diagramService;
    private final Binder<DiagramStylingConfiguration> diagramConfigurationBinder;

    public VariousConfigurationDialog(Diagram diagram, DiagramService diagramService) {
        this.diagramConfigurationBinder = new Binder<>(DiagramStylingConfiguration.class);
        this.diagramService = diagramService;
        this.diagram = diagram;

        setHeaderTitle("Configuration | Various");
        setWidth("40%");
        setHeight("60%");

        add(createDialogLayout());
        diagramConfigurationBinder.readBean(diagram.getDiagramStylingConfiguration());

        getFooter().add(createSaveButton());
        getFooter().add(createCancelButton());
    }

    private Button createSaveButton() {
        Button saveButton = new Button("Save");

        saveButton.addClickListener(e -> {
            diagramConfigurationBinder.writeBeanIfValid(diagram.getDiagramStylingConfiguration());
            diagramService.save(diagram);
            ComponentUtil.fireEvent(UI.getCurrent(), new DiagramStylingChangedEvent(this, false));
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

        Select<Font> fontSelect = new Select<>();
        fontSelect.setItems(Font.values());
        fontSelect.setItemLabelGenerator(Font::getDisplayValue);
        formLayout.addFormItem(fontSelect,"Font");
        diagramConfigurationBinder.forField(fontSelect).bind(DiagramStylingConfiguration::getFont, DiagramStylingConfiguration::setFont);

        Select<Direction> directionSelect = new Select<>();
        directionSelect.setItems(Direction.values());
        directionSelect.setItemLabelGenerator(Direction::getDisplayValue);
        formLayout.addFormItem(directionSelect,"Direction");
        diagramConfigurationBinder.forField(directionSelect).bind(DiagramStylingConfiguration::getDirection, DiagramStylingConfiguration::setDirection);

        Select<Ranker> rankerSelect = new Select<>();
        rankerSelect.setItems(Ranker.values());
        rankerSelect.setItemLabelGenerator(Ranker::getDisplayValue);
        formLayout.addFormItem(rankerSelect,"Ranker");
        diagramConfigurationBinder.forField(rankerSelect).bind(DiagramStylingConfiguration::getRanker, DiagramStylingConfiguration::setRanker);

        Select<Acycler> acyclerSelect = new Select<>();
        acyclerSelect.setItems(Acycler.values());
        acyclerSelect.setItemLabelGenerator(Acycler::getDisplayValue);
        formLayout.addFormItem(acyclerSelect,"Acycler");
        diagramConfigurationBinder.forField(acyclerSelect).bind(DiagramStylingConfiguration::getAcycler, DiagramStylingConfiguration::setAcycler);

        return formLayout;
    }
}
