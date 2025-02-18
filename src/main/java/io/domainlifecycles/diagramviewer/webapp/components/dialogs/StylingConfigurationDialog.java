package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import io.domainlifecycles.diagramviewer.session.AnalyzedDomainModel;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.components.ColorPickerComponent;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.Styling;

/**
 * Dialog allowing configuration for each DDD building block (i.e. AggregateRoot, Repository, etc.).
 *
 * @author leonvoellinger
 */
public class StylingConfigurationDialog extends Dialog {

    private final AnalyzedDomainModel analyzedDomainModel;

    private Button cancelButton;
    private Button saveButton;

    private ColorPickerComponent aggregateRootColorInput;
    private MultiSelectComboBox<Styling> aggregateRootStylingOptionsSelect;

    private ColorPickerComponent entityColorInput;
    private MultiSelectComboBox<Styling> entityStylingOptionsSelect;

    private ColorPickerComponent valueObjectColorInput;
    private MultiSelectComboBox<Styling> valueObjectStylingOptionsSelect;

    private ColorPickerComponent enumColorInput;
    private MultiSelectComboBox<Styling> enumStylingOptionsSelect;

    private ColorPickerComponent identityColorInput;
    private MultiSelectComboBox<Styling> identityStylingOptionsSelect;

    private ColorPickerComponent domainEventColorInput;
    private MultiSelectComboBox<Styling> domainEventStylingOptionsSelect;

    private ColorPickerComponent domainCommandColorInput;
    private MultiSelectComboBox<Styling> domainCommandStylingOptionsSelect;

    private ColorPickerComponent applicationServiceColorInput;
    private MultiSelectComboBox<Styling> applicationServiceStylingOptionsSelect;

    private ColorPickerComponent domainServiceColorInput;
    private MultiSelectComboBox<Styling> domainServiceStylingOptionsSelect;

    private ColorPickerComponent repositoryColorInput;
    private MultiSelectComboBox<Styling> repositoryStylingOptionsSelect;

    private ColorPickerComponent readModelColorInput;
    private MultiSelectComboBox<Styling> readModelStylingOptionsSelect;

    private ColorPickerComponent queryHandlerColorInput;
    private MultiSelectComboBox<Styling> queryHandlerStylingOptionsSelect;

    private ColorPickerComponent outboundServiceColorInput;
    private MultiSelectComboBox<Styling> outboundServiceStylingOptionsSelect;

    private ColorPickerComponent unspecifiedServiceKindColorInput;
    private MultiSelectComboBox<Styling> unspecifiedServiceKindStylingOptionsSelect;


    public StylingConfigurationDialog(AnalyzedDomainModel analyzedDomainModel) {
        this.analyzedDomainModel = analyzedDomainModel;
        setHeaderTitle("Configuration | Styling");

        add(createDialogLayout());

        getFooter().add(createSaveButton());
        getFooter().add(createCancelButton());
    }

    private Button createSaveButton() {
        saveButton = new Button("Save");

        saveButton.addClickListener(e -> {
            //diagramConfigurationBinder.writeBeanIfValid(analyzedDomainModel.getDiagramConfiguration());
            close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return saveButton;
    }

    private Button createCancelButton() {
        cancelButton = new Button("Cancel", e -> close());
        return cancelButton;
    }

    private Accordion createDialogLayout() {
        Accordion accordion = new Accordion();
        accordion.setSizeFull();

        accordion.add(createAndGetAggregateRootAccordionPanel());
        accordion.add(createAndGetEntityAccordionPanel());
        accordion.add(createAndGetValueObjectAccordionPanel());
        accordion.add(createAndGetEnumAccordionPanel());
        accordion.add(createAndGetIdentityAccordionPanel());
        accordion.add(createAndGetDomainEventAccordionPanel());
        accordion.add(createAndGetDomainCommandAccordionPanel());
        accordion.add(createAndGetApplicationServiceAccordionPanel());
        accordion.add(createAndGetDomainServiceAccordionPanel());
        accordion.add(createAndGetRepositoryAccordionPanel());
        accordion.add(createAndGetReadModelAccordionPanel());
        accordion.add(createAndGetQueryHandlerAccordionPanel());
        accordion.add(createAndGetOutboundServiceAccordionPanel());
        accordion.add(createAndGetUnspecifiedServiceKindAccordionPanel());

        return accordion;
    }

    private AccordionPanel createAndGetAggregateRootAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Aggregate Root");

        FormLayout aggregateRootDialogFormLayout = new FormLayout();

        aggregateRootColorInput = new ColorPickerComponent("#8F8F8F");
        aggregateRootDialogFormLayout.addFormItem(aggregateRootColorInput, "Color");

        aggregateRootStylingOptionsSelect = new MultiSelectComboBox<>();
        aggregateRootStylingOptionsSelect.setItems(Styling.values());
        aggregateRootStylingOptionsSelect.setValue(Styling.BOLD);
        aggregateRootStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        aggregateRootDialogFormLayout.addFormItem(aggregateRootStylingOptionsSelect, "Styling Options");

        accordionPanel.add(aggregateRootDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetEntityAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Entity");

        FormLayout entityDialogFormLayout = new FormLayout();

        entityColorInput = new ColorPickerComponent("#88AAFF");
        entityDialogFormLayout.addFormItem(entityColorInput, "Color");

        entityStylingOptionsSelect = new MultiSelectComboBox<>();
        entityStylingOptionsSelect.setItems(Styling.values());
        entityStylingOptionsSelect.setValue(Styling.BOLD);
        entityStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        entityDialogFormLayout.addFormItem(entityStylingOptionsSelect, "Styling Options");

        accordionPanel.add(entityDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetValueObjectAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Value Object");

        FormLayout valueObjectDialogFormLayout = new FormLayout();

        valueObjectColorInput = new ColorPickerComponent("#FFFFCC");
        valueObjectDialogFormLayout.addFormItem(valueObjectColorInput, "Color");

        valueObjectStylingOptionsSelect = new MultiSelectComboBox<>();
        valueObjectStylingOptionsSelect.setItems(Styling.values());
        valueObjectStylingOptionsSelect.setValue(Styling.BOLD);
        valueObjectStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        valueObjectDialogFormLayout.addFormItem(valueObjectStylingOptionsSelect, "Styling Options");

        accordionPanel.add(valueObjectDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetEnumAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Enum");

        FormLayout enumDialogFormLayout = new FormLayout();

        enumColorInput = new ColorPickerComponent("#FFFFCC");
        enumDialogFormLayout.addFormItem(enumColorInput, "Color");

        enumStylingOptionsSelect = new MultiSelectComboBox<>();
        enumStylingOptionsSelect.setItems(Styling.values());
        enumStylingOptionsSelect.setValue(Styling.BOLD);
        enumStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        enumDialogFormLayout.addFormItem(enumStylingOptionsSelect, "Styling Options");

        accordionPanel.add(enumDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetIdentityAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Identity");

        FormLayout identityDialogFormLayout = new FormLayout();

        identityColorInput = new ColorPickerComponent("#FFFFCC");
        identityDialogFormLayout.addFormItem(identityColorInput, "Color");

        identityStylingOptionsSelect = new MultiSelectComboBox<>();
        identityStylingOptionsSelect.setItems(Styling.values());
        identityStylingOptionsSelect.setValue(Styling.BOLD);
        identityStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        identityDialogFormLayout.addFormItem(identityStylingOptionsSelect, "Styling Options");

        accordionPanel.add(identityDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetDomainEventAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Domain Event");

        FormLayout domainEventDialogFormLayout = new FormLayout();

        domainEventColorInput = new ColorPickerComponent("#CCFFFF");
        domainEventDialogFormLayout.addFormItem(domainEventColorInput, "Color");

        domainEventStylingOptionsSelect = new MultiSelectComboBox<>();
        domainEventStylingOptionsSelect.setItems(Styling.values());
        domainEventStylingOptionsSelect.setValue(Styling.BOLD);
        domainEventStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        domainEventDialogFormLayout.addFormItem(domainEventStylingOptionsSelect, "Styling Options");

        accordionPanel.add(domainEventDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetDomainCommandAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Domain Command");

        FormLayout domainCommandDialogFormLayout = new FormLayout();

        domainCommandColorInput = new ColorPickerComponent("#FFB266");
        domainCommandDialogFormLayout.addFormItem(domainCommandColorInput, "Color");

        domainCommandStylingOptionsSelect = new MultiSelectComboBox<>();
        domainCommandStylingOptionsSelect.setItems(Styling.values());
        domainCommandStylingOptionsSelect.setValue(Styling.BOLD);
        domainCommandStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        domainCommandDialogFormLayout.addFormItem(domainCommandStylingOptionsSelect, "Styling Options");

        accordionPanel.add(domainCommandDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetApplicationServiceAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Application Service");

        FormLayout applicationServiceDialogFormLayout = new FormLayout();

        applicationServiceColorInput = new ColorPickerComponent("");
        applicationServiceDialogFormLayout.addFormItem(applicationServiceColorInput, "Color");

        applicationServiceStylingOptionsSelect = new MultiSelectComboBox<>();
        applicationServiceStylingOptionsSelect.setItems(Styling.values());
        applicationServiceStylingOptionsSelect.setValue(Styling.BOLD);
        applicationServiceStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        applicationServiceDialogFormLayout.addFormItem(applicationServiceStylingOptionsSelect, "Styling Options");

        accordionPanel.add(applicationServiceDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetDomainServiceAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Domain Service");

        FormLayout domainServiceDialogFormLayout = new FormLayout();

        domainServiceColorInput = new ColorPickerComponent("#E0E0E0");
        domainServiceDialogFormLayout.addFormItem(domainServiceColorInput, "Color");

        domainServiceStylingOptionsSelect = new MultiSelectComboBox<>();
        domainServiceStylingOptionsSelect.setItems(Styling.values());
        domainServiceStylingOptionsSelect.setValue(Styling.BOLD);
        domainServiceStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        domainServiceDialogFormLayout.addFormItem(domainServiceStylingOptionsSelect, "Styling Options");

        accordionPanel.add(domainServiceDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetRepositoryAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Repository");

        FormLayout repositoryDialogFormLayout = new FormLayout();

        repositoryColorInput = new ColorPickerComponent("#C0C0C0");
        repositoryDialogFormLayout.addFormItem(repositoryColorInput, "Color");

        repositoryStylingOptionsSelect = new MultiSelectComboBox<>();
        repositoryStylingOptionsSelect.setItems(Styling.values());
        repositoryStylingOptionsSelect.setValue(Styling.BOLD);
        repositoryStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        repositoryDialogFormLayout.addFormItem(repositoryStylingOptionsSelect, "Styling Options");

        accordionPanel.add(repositoryDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetReadModelAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Read Model");

        FormLayout readModelDialogFormLayout = new FormLayout();

        readModelColorInput = new ColorPickerComponent("#FFCCE5");
        readModelDialogFormLayout.addFormItem(readModelColorInput, "Color");

        readModelStylingOptionsSelect = new MultiSelectComboBox<>();
        readModelStylingOptionsSelect.setItems(Styling.values());
        readModelStylingOptionsSelect.setValue(Styling.BOLD);
        readModelStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        readModelDialogFormLayout.addFormItem(readModelStylingOptionsSelect, "Styling Options");

        accordionPanel.add(readModelDialogFormLayout);
        return accordionPanel;
    }
    private AccordionPanel createAndGetQueryHandlerAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Query Handler");

        FormLayout queryHandlerDialogFormLayout = new FormLayout();

        queryHandlerColorInput = new ColorPickerComponent("#C0C0C0");
        queryHandlerDialogFormLayout.addFormItem(queryHandlerColorInput, "Color");

        queryHandlerStylingOptionsSelect = new MultiSelectComboBox<>();
        queryHandlerStylingOptionsSelect.setItems(Styling.values());
        queryHandlerStylingOptionsSelect.setValue(Styling.BOLD);
        queryHandlerStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        queryHandlerDialogFormLayout.addFormItem(queryHandlerStylingOptionsSelect, "Styling Options");

        accordionPanel.add(queryHandlerDialogFormLayout);
        return accordionPanel;
    }
    private AccordionPanel createAndGetOutboundServiceAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Outbound Service");

        FormLayout outboundServiceDialogFormLayout = new FormLayout();

        outboundServiceColorInput = new ColorPickerComponent("#C0C0C0");
        outboundServiceDialogFormLayout.addFormItem(outboundServiceColorInput, "Color");

        outboundServiceStylingOptionsSelect = new MultiSelectComboBox<>();
        outboundServiceStylingOptionsSelect.setItems(Styling.values());
        outboundServiceStylingOptionsSelect.setValue(Styling.BOLD);
        outboundServiceStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        outboundServiceDialogFormLayout.addFormItem(outboundServiceStylingOptionsSelect, "Styling Options");

        accordionPanel.add(outboundServiceDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetUnspecifiedServiceKindAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Service Kind");

        FormLayout unspecifiedServiceKindDialogFormLayout = new FormLayout();

        unspecifiedServiceKindColorInput = new ColorPickerComponent("#C0C0C0");
        unspecifiedServiceKindDialogFormLayout.addFormItem(unspecifiedServiceKindColorInput, "Color");

        unspecifiedServiceKindStylingOptionsSelect = new MultiSelectComboBox<>();
        unspecifiedServiceKindStylingOptionsSelect.setItems(Styling.values());
        unspecifiedServiceKindStylingOptionsSelect.setValue(Styling.BOLD);
        unspecifiedServiceKindStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        unspecifiedServiceKindDialogFormLayout.addFormItem(unspecifiedServiceKindStylingOptionsSelect, "Styling Options");

        accordionPanel.add(unspecifiedServiceKindDialogFormLayout);
        return accordionPanel;
    }
}
