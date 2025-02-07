package io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.components.ColorPickerComponent;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.values.StylingOptions;

/**
 * Dialog allowing configuration for each DDD building block (i.e. AggregateRoot, Repository, etc.).
 *
 * @author leonvoellinger
 */
public class StylingConfigurationDialog extends Dialog {

    private Button cancelButton;
    private Button saveButton;

    private ColorPickerComponent aggregateRootColorInput;
    private MultiSelectComboBox<StylingOptions> aggregateRootStylingOptionsSelect;

    private ColorPickerComponent entityColorInput;
    private MultiSelectComboBox<StylingOptions> entityStylingOptionsSelect;

    private ColorPickerComponent valueObjectColorInput;
    private MultiSelectComboBox<StylingOptions> valueObjectStylingOptionsSelect;

    private ColorPickerComponent enumColorInput;
    private MultiSelectComboBox<StylingOptions> enumStylingOptionsSelect;

    private ColorPickerComponent identityColorInput;
    private MultiSelectComboBox<StylingOptions> identityStylingOptionsSelect;

    private ColorPickerComponent domainEventColorInput;
    private MultiSelectComboBox<StylingOptions> domainEventStylingOptionsSelect;

    private ColorPickerComponent domainCommandColorInput;
    private MultiSelectComboBox<StylingOptions> domainCommandStylingOptionsSelect;

    private ColorPickerComponent applicationServiceColorInput;
    private MultiSelectComboBox<StylingOptions> applicationServiceStylingOptionsSelect;

    private ColorPickerComponent domainServiceColorInput;
    private MultiSelectComboBox<StylingOptions> domainServiceStylingOptionsSelect;

    private ColorPickerComponent repositoryColorInput;
    private MultiSelectComboBox<StylingOptions> repositoryStylingOptionsSelect;

    private ColorPickerComponent readModelColorInput;
    private MultiSelectComboBox<StylingOptions> readModelStylingOptionsSelect;

    private ColorPickerComponent queryHandlerColorInput;
    private MultiSelectComboBox<StylingOptions> queryHandlerStylingOptionsSelect;

    private ColorPickerComponent outboundServiceColorInput;
    private MultiSelectComboBox<StylingOptions> outboundServiceStylingOptionsSelect;

    private ColorPickerComponent unspecifiedServiceKindColorInput;
    private MultiSelectComboBox<StylingOptions> unspecifiedServiceKindStylingOptionsSelect;


    public StylingConfigurationDialog() {
        setHeaderTitle("Configuration | Styling");

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
        aggregateRootStylingOptionsSelect.setItems(StylingOptions.values());
        aggregateRootStylingOptionsSelect.setValue(StylingOptions.BOLD);
        aggregateRootStylingOptionsSelect.setItemLabelGenerator(StylingOptions::getDisplayValue);
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
        entityStylingOptionsSelect.setItems(StylingOptions.values());
        entityStylingOptionsSelect.setValue(StylingOptions.BOLD);
        entityStylingOptionsSelect.setItemLabelGenerator(StylingOptions::getDisplayValue);
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
        valueObjectStylingOptionsSelect.setItems(StylingOptions.values());
        valueObjectStylingOptionsSelect.setValue(StylingOptions.BOLD);
        valueObjectStylingOptionsSelect.setItemLabelGenerator(StylingOptions::getDisplayValue);
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
        enumStylingOptionsSelect.setItems(StylingOptions.values());
        enumStylingOptionsSelect.setValue(StylingOptions.BOLD);
        enumStylingOptionsSelect.setItemLabelGenerator(StylingOptions::getDisplayValue);
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
        identityStylingOptionsSelect.setItems(StylingOptions.values());
        identityStylingOptionsSelect.setValue(StylingOptions.BOLD);
        identityStylingOptionsSelect.setItemLabelGenerator(StylingOptions::getDisplayValue);
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
        domainEventStylingOptionsSelect.setItems(StylingOptions.values());
        domainEventStylingOptionsSelect.setValue(StylingOptions.BOLD);
        domainEventStylingOptionsSelect.setItemLabelGenerator(StylingOptions::getDisplayValue);
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
        domainCommandStylingOptionsSelect.setItems(StylingOptions.values());
        domainCommandStylingOptionsSelect.setValue(StylingOptions.BOLD);
        domainCommandStylingOptionsSelect.setItemLabelGenerator(StylingOptions::getDisplayValue);
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
        applicationServiceStylingOptionsSelect.setItems(StylingOptions.values());
        applicationServiceStylingOptionsSelect.setValue(StylingOptions.BOLD);
        applicationServiceStylingOptionsSelect.setItemLabelGenerator(StylingOptions::getDisplayValue);
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
        domainServiceStylingOptionsSelect.setItems(StylingOptions.values());
        domainServiceStylingOptionsSelect.setValue(StylingOptions.BOLD);
        domainServiceStylingOptionsSelect.setItemLabelGenerator(StylingOptions::getDisplayValue);
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
        repositoryStylingOptionsSelect.setItems(StylingOptions.values());
        repositoryStylingOptionsSelect.setValue(StylingOptions.BOLD);
        repositoryStylingOptionsSelect.setItemLabelGenerator(StylingOptions::getDisplayValue);
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
        readModelStylingOptionsSelect.setItems(StylingOptions.values());
        readModelStylingOptionsSelect.setValue(StylingOptions.BOLD);
        readModelStylingOptionsSelect.setItemLabelGenerator(StylingOptions::getDisplayValue);
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
        queryHandlerStylingOptionsSelect.setItems(StylingOptions.values());
        queryHandlerStylingOptionsSelect.setValue(StylingOptions.BOLD);
        queryHandlerStylingOptionsSelect.setItemLabelGenerator(StylingOptions::getDisplayValue);
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
        outboundServiceStylingOptionsSelect.setItems(StylingOptions.values());
        outboundServiceStylingOptionsSelect.setValue(StylingOptions.BOLD);
        outboundServiceStylingOptionsSelect.setItemLabelGenerator(StylingOptions::getDisplayValue);
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
        unspecifiedServiceKindStylingOptionsSelect.setItems(StylingOptions.values());
        unspecifiedServiceKindStylingOptionsSelect.setValue(StylingOptions.BOLD);
        unspecifiedServiceKindStylingOptionsSelect.setItemLabelGenerator(StylingOptions::getDisplayValue);
        unspecifiedServiceKindDialogFormLayout.addFormItem(unspecifiedServiceKindStylingOptionsSelect, "Styling Options");

        accordionPanel.add(unspecifiedServiceKindDialogFormLayout);
        return accordionPanel;
    }
}
