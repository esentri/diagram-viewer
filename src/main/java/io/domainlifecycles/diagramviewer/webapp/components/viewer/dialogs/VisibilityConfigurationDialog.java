package io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.components.ColorPickerComponent;
import io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.values.StylingOptions;

public class VisibilityConfigurationDialog extends Dialog {

    private Button cancelButton;
    private Button saveButton;

    // general
    private Checkbox showAllFieldsCheckbox;
    private Checkbox showAllMethodsCheckbox;
    private Checkbox showFullQualifiedClassNamesCheckbox;
    private Checkbox showAssertionsCheckbox;
    private Checkbox showOnlyPublicMethodsCheckbox;

    // specific
    private Checkbox showDomainEventsCheckbox;
    private Checkbox showDomainEventFieldsCheckbox;
    private Checkbox showDomainEventMethodsCheckbox;

    private Checkbox showDomainCommandsCheckbox;
    private Checkbox showDomainCommandFieldsCheckbox;
    private Checkbox showDomainCommandMethodsCheckbox;
    private Checkbox showOnlyTopLevelDomainCommandRelationCheckbox;

    private Checkbox showDomainServicesCheckbox;
    private Checkbox showDomainServiceFieldsCheckbox;
    private Checkbox showDomainServiceMethodsCheckbox;

    private Checkbox showApplicationServicesCheckbox;
    private Checkbox showApplicationServiceFieldsCheckbox;
    private Checkbox showApplicationServiceMethodsCheckbox;

    private Checkbox showRepositoriesCheckbox;
    private Checkbox showRepositoryFieldsCheckbox;
    private Checkbox showRepositoryMethodsCheckbox;

    private Checkbox showReadModelsCheckbox;
    private Checkbox showReadModelFieldsCheckbox;
    private Checkbox showReadModelMethodsCheckbox;

    private Checkbox showQueryHandlersCheckbox;
    private Checkbox showQueryHandlerFieldsCheckbox;
    private Checkbox showQueryHandlerMethodsCheckbox;

    private Checkbox showOutboundServicesCheckbox;
    private Checkbox showOutboundServiceFieldsCheckbox;
    private Checkbox showOutboundServiceMethodsCheckbox;

    private Checkbox showUnspecifiedServiceKindsCheckbox;
    private Checkbox showUnspecifiedServiceKindFieldsCheckbox;
    private Checkbox showUnspecifiedServiceKindMethodsCheckbox;

    public VisibilityConfigurationDialog() {
        setHeaderTitle("Configuration | Visibility");

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

        accordion.add(createAndGetGeneralAccordionPanel());
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

    private AccordionPanel createAndGetGeneralAccordionPanel() {
        AccordionPanel generalPanel = new AccordionPanel();
        generalPanel.setSummaryText("General");

        FormLayout formLayout = new FormLayout();

        showAllFieldsCheckbox = new Checkbox();
        formLayout.addFormItem(showAllFieldsCheckbox,"Fields");
        showAllFieldsCheckbox.setValue(true);

        showAllMethodsCheckbox = new Checkbox();
        formLayout.addFormItem(showAllMethodsCheckbox,"Methods");
        showAllMethodsCheckbox.setValue(true);
        showAllMethodsCheckbox.addClickListener(event -> showOnlyPublicMethodsCheckbox.setEnabled(showAllMethodsCheckbox.getValue()));

        showFullQualifiedClassNamesCheckbox= new Checkbox();
        formLayout.addFormItem(showFullQualifiedClassNamesCheckbox,"Full qualified class names");
        showFullQualifiedClassNamesCheckbox.setValue(true);

        showAssertionsCheckbox = new Checkbox();
        formLayout.addFormItem(showAssertionsCheckbox,"Assertions");
        showAssertionsCheckbox.setValue(true);

        showOnlyPublicMethodsCheckbox = new Checkbox();
        formLayout.addFormItem(showOnlyPublicMethodsCheckbox,"Public methods only");
        showOnlyPublicMethodsCheckbox.setValue(true);

        generalPanel.add(formLayout);
        return generalPanel;
    }

    private AccordionPanel createAndGetDomainEventAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Domain Events");

        FormLayout domainEventDialogFormLayout = new FormLayout();

        showDomainEventsCheckbox = new Checkbox();
        showDomainEventsCheckbox.setValue(true);

        showDomainEventFieldsCheckbox = new Checkbox();
        showDomainEventFieldsCheckbox.setValue(true);
        domainEventDialogFormLayout.setColspan(showDomainEventFieldsCheckbox, 1);

        showDomainEventMethodsCheckbox = new Checkbox();
        showDomainEventMethodsCheckbox.setValue(true);
        domainEventDialogFormLayout.setColspan(showDomainEventMethodsCheckbox, 1);

        domainEventDialogFormLayout.addFormItem(showDomainEventsCheckbox,"Show");
        domainEventDialogFormLayout.addFormItem(showDomainEventFieldsCheckbox,"Fields");
        domainEventDialogFormLayout.addFormItem(showDomainEventMethodsCheckbox,"Methods");

        accordionPanel.add(domainEventDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetDomainCommandAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Domain Command");

        FormLayout domainCommandDialogFormLayout = new FormLayout();

        showDomainCommandsCheckbox = new Checkbox();
        showDomainCommandsCheckbox.setValue(true);

        showDomainCommandFieldsCheckbox = new Checkbox();
        showDomainCommandFieldsCheckbox.setValue(true);

        showDomainCommandMethodsCheckbox = new Checkbox();
        showDomainCommandMethodsCheckbox.setValue(true);

        domainCommandDialogFormLayout.addFormItem(showDomainCommandsCheckbox,"Show");
        domainCommandDialogFormLayout.addFormItem(showDomainCommandFieldsCheckbox,"Fields");
        domainCommandDialogFormLayout.addFormItem(showDomainCommandMethodsCheckbox,"Methods");

        accordionPanel.add(domainCommandDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetApplicationServiceAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Application Service");

        FormLayout applicationServiceDialogFormLayout = new FormLayout();

        showApplicationServicesCheckbox = new Checkbox();
        showApplicationServicesCheckbox.setValue(true);

        showApplicationServiceFieldsCheckbox = new Checkbox();
        showApplicationServiceFieldsCheckbox.setValue(true);

        showApplicationServiceMethodsCheckbox = new Checkbox();
        showApplicationServiceMethodsCheckbox.setValue(true);

        applicationServiceDialogFormLayout.addFormItem(showApplicationServicesCheckbox,"Show");
        applicationServiceDialogFormLayout.addFormItem(showApplicationServiceFieldsCheckbox,"Fields");
        applicationServiceDialogFormLayout.addFormItem(showApplicationServiceMethodsCheckbox,"Methods");

        accordionPanel.add(applicationServiceDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetDomainServiceAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Domain Service");

        FormLayout domainServiceDialogFormLayout = new FormLayout();

        showDomainServicesCheckbox = new Checkbox();
        showDomainServicesCheckbox.setValue(true);

        showDomainServiceFieldsCheckbox = new Checkbox();
        showDomainServiceFieldsCheckbox.setValue(true);

        showDomainServiceMethodsCheckbox = new Checkbox();
        showDomainServiceMethodsCheckbox.setValue(true);

        domainServiceDialogFormLayout.addFormItem(showApplicationServicesCheckbox,"Show");
        domainServiceDialogFormLayout.addFormItem(showApplicationServiceFieldsCheckbox,"Fields");
        domainServiceDialogFormLayout.addFormItem(showApplicationServiceMethodsCheckbox,"Methods");

        accordionPanel.add(domainServiceDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetRepositoryAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Repository");

        FormLayout repositoryDialogFormLayout = new FormLayout();

        showRepositoriesCheckbox = new Checkbox();
        showRepositoriesCheckbox.setValue(true);

        showRepositoryFieldsCheckbox = new Checkbox();
        showRepositoryFieldsCheckbox.setValue(true);

        showRepositoryMethodsCheckbox = new Checkbox();
        showRepositoryMethodsCheckbox.setValue(true);

        repositoryDialogFormLayout.addFormItem(showRepositoriesCheckbox,"Show");
        repositoryDialogFormLayout.addFormItem(showRepositoryFieldsCheckbox,"Fields");
        repositoryDialogFormLayout.addFormItem(showRepositoryMethodsCheckbox,"Methods");

        accordionPanel.add(repositoryDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetReadModelAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Read Model");

        FormLayout readModelDialogFormLayout = new FormLayout();

        showReadModelsCheckbox = new Checkbox();
        showReadModelsCheckbox.setValue(true);

        showReadModelFieldsCheckbox = new Checkbox();
        showReadModelFieldsCheckbox.setValue(true);

        showReadModelMethodsCheckbox = new Checkbox();
        showReadModelMethodsCheckbox.setValue(true);

        readModelDialogFormLayout.addFormItem(showReadModelsCheckbox,"Show");
        readModelDialogFormLayout.addFormItem(showReadModelFieldsCheckbox,"Fields");
        readModelDialogFormLayout.addFormItem(showReadModelMethodsCheckbox,"Methods");

        accordionPanel.add(readModelDialogFormLayout);
        return accordionPanel;
    }
    private AccordionPanel createAndGetQueryHandlerAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Query Handler");

        FormLayout queryHandlerDialogFormLayout = new FormLayout();

        showQueryHandlersCheckbox = new Checkbox();
        showQueryHandlersCheckbox.setValue(true);

        showQueryHandlerFieldsCheckbox = new Checkbox();
        showQueryHandlerFieldsCheckbox.setValue(true);

        showQueryHandlerMethodsCheckbox = new Checkbox();
        showQueryHandlerMethodsCheckbox.setValue(true);

        queryHandlerDialogFormLayout.addFormItem(showQueryHandlersCheckbox,"Show");
        queryHandlerDialogFormLayout.addFormItem(showQueryHandlerFieldsCheckbox,"Fields");
        queryHandlerDialogFormLayout.addFormItem(showQueryHandlerMethodsCheckbox,"Methods");

        accordionPanel.add(queryHandlerDialogFormLayout);
        return accordionPanel;
    }
    private AccordionPanel createAndGetOutboundServiceAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Outbound Service");

        FormLayout outboundServiceDialogFormLayout = new FormLayout();

        showOutboundServicesCheckbox = new Checkbox();
        showOutboundServicesCheckbox.setValue(true);

        showOutboundServiceFieldsCheckbox = new Checkbox();
        showOutboundServiceFieldsCheckbox.setValue(true);

        showOutboundServiceMethodsCheckbox = new Checkbox();
        showOutboundServiceMethodsCheckbox.setValue(true);

        outboundServiceDialogFormLayout.addFormItem(showOutboundServicesCheckbox,"Show");
        outboundServiceDialogFormLayout.addFormItem(showOutboundServiceFieldsCheckbox,"Fields");
        outboundServiceDialogFormLayout.addFormItem(showOutboundServiceMethodsCheckbox,"Methods");

        accordionPanel.add(outboundServiceDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetUnspecifiedServiceKindAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Service Kind");

        FormLayout unspecifiedServiceKindDialogFormLayout = new FormLayout();

        showUnspecifiedServiceKindsCheckbox = new Checkbox();
        showUnspecifiedServiceKindsCheckbox.setValue(true);

        showUnspecifiedServiceKindFieldsCheckbox = new Checkbox();
        showUnspecifiedServiceKindFieldsCheckbox.setValue(true);

        showUnspecifiedServiceKindMethodsCheckbox = new Checkbox();
        showUnspecifiedServiceKindMethodsCheckbox.setValue(true);

        unspecifiedServiceKindDialogFormLayout.addFormItem(showUnspecifiedServiceKindsCheckbox,"Show");
        unspecifiedServiceKindDialogFormLayout.addFormItem(showUnspecifiedServiceKindFieldsCheckbox,"Fields");
        unspecifiedServiceKindDialogFormLayout.addFormItem(showUnspecifiedServiceKindMethodsCheckbox,"Methods");

        accordionPanel.add(unspecifiedServiceKindDialogFormLayout);
        return accordionPanel;
    }
}
