package io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.Binder;
import io.domainlifecycles.diagramviewer.model.DiagramConfiguration;
import io.domainlifecycles.diagramviewer.session.AnalyzedDomainModel;
import org.atmosphere.interceptor.AtmosphereResourceStateRecovery.B;

public class VisibilityConfigurationDialog extends Dialog {

    private final Binder<DiagramConfiguration> diagramConfigurationBinder;
    private final AnalyzedDomainModel analyzedDomainModel;

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

    public VisibilityConfigurationDialog(AnalyzedDomainModel analyzedDomainModel) {
        this.analyzedDomainModel = analyzedDomainModel;
        diagramConfigurationBinder = new Binder<>(DiagramConfiguration.class);

        setHeaderTitle("Configuration | Visibility");

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
        diagramConfigurationBinder.bind(showAllFieldsCheckbox, DiagramConfiguration::isShowFields, DiagramConfiguration::setShowFields);

        showAllMethodsCheckbox = new Checkbox();
        formLayout.addFormItem(showAllMethodsCheckbox,"Methods");
        showAllMethodsCheckbox.addClickListener(event -> showOnlyPublicMethodsCheckbox.setEnabled(showAllMethodsCheckbox.getValue()));
        diagramConfigurationBinder.bind(showAllMethodsCheckbox, DiagramConfiguration::isShowMethods, DiagramConfiguration::setShowMethods);

        showFullQualifiedClassNamesCheckbox= new Checkbox();
        formLayout.addFormItem(showFullQualifiedClassNamesCheckbox,"Full qualified class names");
        diagramConfigurationBinder.bind(showFullQualifiedClassNamesCheckbox, DiagramConfiguration::isShowFullQualifiedClassNames, DiagramConfiguration::setShowFullQualifiedClassNames);

        showAssertionsCheckbox = new Checkbox();
        formLayout.addFormItem(showAssertionsCheckbox,"Assertions");
        diagramConfigurationBinder.bind(showAssertionsCheckbox, DiagramConfiguration::isShowAssertions, DiagramConfiguration::setShowAssertions);

        showOnlyPublicMethodsCheckbox = new Checkbox();
        formLayout.addFormItem(showOnlyPublicMethodsCheckbox,"Public methods only");
        diagramConfigurationBinder.bind(showOnlyPublicMethodsCheckbox, DiagramConfiguration::isShowOnlyPublicMethods, DiagramConfiguration::setShowOnlyPublicMethods);

        generalPanel.add(formLayout);
        return generalPanel;
    }

    private AccordionPanel createAndGetDomainEventAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Domain Events");

        FormLayout domainEventDialogFormLayout = new FormLayout();

        showDomainEventsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showDomainEventsCheckbox, DiagramConfiguration::isShowDomainEvents, DiagramConfiguration::setShowDomainEvents);

        showDomainEventFieldsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showDomainEventFieldsCheckbox, DiagramConfiguration::isShowDomainEventFields, DiagramConfiguration::setShowDomainEventFields);

        showDomainEventMethodsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showDomainEventMethodsCheckbox, DiagramConfiguration::isShowDomainEventMethods, DiagramConfiguration::setShowDomainEventMethods);

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
        diagramConfigurationBinder.bind(showDomainCommandsCheckbox, DiagramConfiguration::isShowDomainCommands, DiagramConfiguration::setShowDomainCommands);

        showDomainCommandFieldsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showDomainCommandFieldsCheckbox, DiagramConfiguration::isShowDomainCommandFields, DiagramConfiguration::setShowDomainCommandFields);

        showDomainCommandMethodsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showDomainCommandMethodsCheckbox, DiagramConfiguration::isShowDomainCommandMethods, DiagramConfiguration::setShowDomainCommandMethods);

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
        diagramConfigurationBinder.bind(showApplicationServicesCheckbox, DiagramConfiguration::isShowApplicationServices, DiagramConfiguration::setShowApplicationServices);

        showApplicationServiceFieldsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showApplicationServiceFieldsCheckbox, DiagramConfiguration::isShowApplicationServiceFields, DiagramConfiguration::setShowApplicationServiceFields);

        showApplicationServiceMethodsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showApplicationServiceMethodsCheckbox, DiagramConfiguration::isShowApplicationServiceMethods, DiagramConfiguration::setShowApplicationServiceMethods);

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
        diagramConfigurationBinder.bind(showDomainServicesCheckbox, DiagramConfiguration::isShowDomainServices, DiagramConfiguration::setShowDomainServices);

        showDomainServiceFieldsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showDomainServiceFieldsCheckbox, DiagramConfiguration::isShowDomainServiceFields, DiagramConfiguration::setShowDomainServiceFields);

        showDomainServiceMethodsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showDomainServiceMethodsCheckbox, DiagramConfiguration::isShowDomainServiceMethods, DiagramConfiguration::setShowDomainServiceMethods);

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
        diagramConfigurationBinder.bind(showRepositoriesCheckbox, DiagramConfiguration::isShowRepositories, DiagramConfiguration::setShowRepositories);

        showRepositoryFieldsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showRepositoryFieldsCheckbox, DiagramConfiguration::isShowRepositoryFields, DiagramConfiguration::setShowRepositoryFields);

        showRepositoryMethodsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showRepositoryMethodsCheckbox, DiagramConfiguration::isShowRepositoryMethods, DiagramConfiguration::setShowRepositoryMethods);

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
        diagramConfigurationBinder.bind(showReadModelsCheckbox, DiagramConfiguration::isShowReadModels, DiagramConfiguration::setShowReadModels);

        showReadModelFieldsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showReadModelFieldsCheckbox, DiagramConfiguration::isShowReadModelFields, DiagramConfiguration::setShowReadModelFields);

        showReadModelMethodsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showReadModelMethodsCheckbox, DiagramConfiguration::isShowReadModelMethods, DiagramConfiguration::setShowReadModelMethods);

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
        diagramConfigurationBinder.bind(showQueryHandlersCheckbox, DiagramConfiguration::isShowQueryHandlers, DiagramConfiguration::setShowQueryHandlers);

        showQueryHandlerFieldsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showQueryHandlerFieldsCheckbox, DiagramConfiguration::isShowQueryHandlerFields, DiagramConfiguration::setShowQueryHandlerFields);

        showQueryHandlerMethodsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showQueryHandlerMethodsCheckbox, DiagramConfiguration::isShowQueryHandlerMethods, DiagramConfiguration::setShowQueryHandlerMethods);

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
        diagramConfigurationBinder.bind(showOutboundServicesCheckbox, DiagramConfiguration::isShowOutboundServices, DiagramConfiguration::setShowOutboundServices);

        showOutboundServiceFieldsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showOutboundServiceFieldsCheckbox, DiagramConfiguration::isShowOutboundServiceFields, DiagramConfiguration::setShowOutboundServiceFields);

        showOutboundServiceMethodsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showOutboundServiceMethodsCheckbox, DiagramConfiguration::isShowOutboundServiceMethods, DiagramConfiguration::setShowOutboundServiceMethods);

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
        diagramConfigurationBinder.bind(showUnspecifiedServiceKindsCheckbox, DiagramConfiguration::isShowUnspecifiedServiceKinds, DiagramConfiguration::setShowUnspecifiedServiceKinds);

        showUnspecifiedServiceKindFieldsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showUnspecifiedServiceKindFieldsCheckbox, DiagramConfiguration::isShowUnspecifiedServiceKindFields, DiagramConfiguration::setShowUnspecifiedServiceKindFields);

        showUnspecifiedServiceKindMethodsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showUnspecifiedServiceKindMethodsCheckbox, DiagramConfiguration::isShowUnspecifiedServiceKindMethods, DiagramConfiguration::setShowUnspecifiedServiceKindMethods);

        unspecifiedServiceKindDialogFormLayout.addFormItem(showUnspecifiedServiceKindsCheckbox,"Show");
        unspecifiedServiceKindDialogFormLayout.addFormItem(showUnspecifiedServiceKindFieldsCheckbox,"Fields");
        unspecifiedServiceKindDialogFormLayout.addFormItem(showUnspecifiedServiceKindMethodsCheckbox,"Methods");

        accordionPanel.add(unspecifiedServiceKindDialogFormLayout);
        return accordionPanel;
    }
}
