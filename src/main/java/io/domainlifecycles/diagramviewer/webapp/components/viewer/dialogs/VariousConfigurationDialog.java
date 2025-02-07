package io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.binder.Binder;
import io.domainlifecycles.diagramviewer.model.DiagramConfiguration;
import io.domainlifecycles.diagramviewer.session.AnalyzedDomainModel;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.values.Acycler;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.values.Direction;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.values.Font;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.values.Ranker;

public class VariousConfigurationDialog extends Dialog {

    private final AnalyzedDomainModel analyzedDomainModel;
    private final Binder<DiagramConfiguration> diagramConfigurationBinder;

    private Button cancelButton;
    private Button saveButton;
    private Select<Font> fontSelect;
    private Select<Direction> directionSelect;
    private Select<Ranker> rankerSelect;
    private Select<Acycler> acyclerSelect;

    public VariousConfigurationDialog(AnalyzedDomainModel analyzedDomainModel) {
        this.diagramConfigurationBinder = new Binder<>(DiagramConfiguration.class);
        this.analyzedDomainModel = analyzedDomainModel;

        setHeaderTitle("Configuration | Various");

        add(createDialogLayout());
        diagramConfigurationBinder.readBean(analyzedDomainModel.getDiagramConfiguration());

        getFooter().add(createSaveButton());
        getFooter().add(createCancelButton());
    }

    private Button createSaveButton() {
        saveButton = new Button("Save");

        saveButton.addClickListener(e -> {
            diagramConfigurationBinder.writeBeanIfValid(analyzedDomainModel.getDiagramConfiguration());
            close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return saveButton;
    }

    private Button createCancelButton() {
        cancelButton = new Button("Cancel", e -> {
            diagramConfigurationBinder.readBean(analyzedDomainModel.getDiagramConfiguration());
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
        diagramConfigurationBinder.forField(fontSelect).bind(DiagramConfiguration::getFont, DiagramConfiguration::setFont);

        directionSelect = new Select<>();
        directionSelect.setItems(Direction.values());
        directionSelect.setItemLabelGenerator(Direction::getDisplayValue);
        formLayout.addFormItem(directionSelect,"Direction");
        diagramConfigurationBinder.forField(directionSelect).bind(DiagramConfiguration::getDirection, DiagramConfiguration::setDirection);

        rankerSelect = new Select<>();
        rankerSelect.setItems(Ranker.values());
        rankerSelect.setItemLabelGenerator(Ranker::getDisplayValue);
        formLayout.addFormItem(rankerSelect,"Ranker");
        diagramConfigurationBinder.forField(rankerSelect).bind(DiagramConfiguration::getRanker, DiagramConfiguration::setRanker);

        acyclerSelect = new Select<>();
        acyclerSelect.setItems(Acycler.values());
        acyclerSelect.setItemLabelGenerator(Acycler::getDisplayValue);
        formLayout.addFormItem(acyclerSelect,"Acycler");
        diagramConfigurationBinder.forField(acyclerSelect).bind(DiagramConfiguration::getAcycler, DiagramConfiguration::setAcycler);

        return formLayout;
    }
}
