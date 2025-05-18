package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.Binder;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramStylingChangedEvent;

public class VisibilityConfigurationDialog extends Dialog {

    private final Binder<DiagramStylingConfiguration> diagramConfigurationBinder;
    private final Binder<DomainModelVisibility> domainModelVisibilityBinder;
    private final DiagramService diagramService;
    private final Project project;
    private final Diagram diagram;

    private Checkbox showAllFieldsCheckbox;
    private Checkbox showAllMethodsCheckbox;
    private Checkbox showDomainEventFieldsCheckbox;
    private Checkbox showDomainEventMethodsCheckbox;
    private Checkbox showDomainCommandFieldsCheckbox;
    private Checkbox showDomainCommandMethodsCheckbox;
    private Checkbox showApplicationServiceFieldsCheckbox;
    private Checkbox showApplicationServiceMethodsCheckbox;
    private Checkbox showDomainServiceFieldsCheckbox;
    private Checkbox showDomainServiceMethodsCheckbox;
    private Checkbox showRepositoryFieldsCheckbox;
    private Checkbox showRepositoryMethodsCheckbox;
    private Checkbox showReadModelFieldsCheckbox;
    private Checkbox showReadModelMethodsCheckbox;
    private Checkbox showQueryHandlerFieldsCheckbox;
    private Checkbox showQueryHandlerMethodsCheckbox;
    private Checkbox showOutboundServiceFieldsCheckbox;
    private Checkbox showOutboundServiceMethodsCheckbox;
    private Checkbox showUnspecifiedServiceKindFieldsCheckbox;
    private Checkbox showUnspecifiedServiceKindMethodsCheckbox;
    private Checkbox showAggregateFieldsCheckbox;
    private Checkbox showAggregateMethodsCheckbox;


    public VisibilityConfigurationDialog(DiagramService diagramService, Project project, Diagram diagram) {
        this.diagramService = diagramService;
        this.project = project;
        this.diagram = diagram;
        diagramConfigurationBinder = new Binder<>(DiagramStylingConfiguration.class);
        domainModelVisibilityBinder = new Binder<>(DomainModelVisibility.class);

        setHeaderTitle("Configuration | Visibility");
        setWidth("40%");
        setHeight("60%");

        add(createDialogLayout());

        addOpenedChangeListener(e -> {
            if(e.isOpened()) {
                diagramConfigurationBinder.readBean(diagram.getDiagramStylingConfiguration());
                domainModelVisibilityBinder.readBean(diagram.getDomainModelVisibility());
            }
        });

        getFooter().add(createSaveButton());
        getFooter().add(createCancelButton());
    }

    private Button createSaveButton() {
        Button saveButton = new Button("Save");

        saveButton.addClickListener(e -> {
            diagramConfigurationBinder.writeBeanIfValid(diagram.getDiagramStylingConfiguration());
            domainModelVisibilityBinder.writeBeanIfValid(diagram.getDomainModelVisibility());
            diagramService.update(diagram, project);
            ComponentUtil.fireEvent(UI.getCurrent(), new DiagramStylingChangedEvent(this, false));
            close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return saveButton;
    }

    private Button createCancelButton() {
        return new Button("Cancel", e -> close());
    }

    private Accordion createDialogLayout() {

        Accordion accordion = new Accordion();
        accordion.setSizeFull();

        accordion.add(createAndGetGeneralAccordionPanel());
        accordion.add(createAndGetAggregateAccordionPanel());
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
        // general
        showAllFieldsCheckbox = new Checkbox();
        formLayout.addFormItem(showAllFieldsCheckbox,"Fields");
        diagramConfigurationBinder.bind(showAllFieldsCheckbox, DiagramStylingConfiguration::isShowFields, DiagramStylingConfiguration::setShowFields);
        showAllFieldsCheckbox.addClickListener(event -> invertShowFieldsCheckboxes());

        showAllMethodsCheckbox = new Checkbox();
        formLayout.addFormItem(showAllMethodsCheckbox,"Methods");
        diagramConfigurationBinder.bind(showAllMethodsCheckbox, DiagramStylingConfiguration::isShowMethods, DiagramStylingConfiguration::setShowMethods);
        showAllMethodsCheckbox.addClickListener(event -> invertShowMethodsCheckboxes());

        Checkbox showFullQualifiedClassNamesCheckbox = new Checkbox();
        formLayout.addFormItem(showFullQualifiedClassNamesCheckbox,"Full qualified class names");
        diagramConfigurationBinder.bind(showFullQualifiedClassNamesCheckbox, DiagramStylingConfiguration::isShowFullQualifiedClassNames, DiagramStylingConfiguration::setShowFullQualifiedClassNames);

        Checkbox showAssertionsCheckbox = new Checkbox();
        formLayout.addFormItem(showAssertionsCheckbox,"Assertions");
        diagramConfigurationBinder.bind(showAssertionsCheckbox, DiagramStylingConfiguration::isShowAssertions, DiagramStylingConfiguration::setShowAssertions);

        Checkbox showOnlyPublicMethodsCheckbox = new Checkbox();
        formLayout.addFormItem(showOnlyPublicMethodsCheckbox,"Public methods only");
        diagramConfigurationBinder.bind(showOnlyPublicMethodsCheckbox, DiagramStylingConfiguration::isShowOnlyPublicMethods, DiagramStylingConfiguration::setShowOnlyPublicMethods);

        generalPanel.add(formLayout);
        return generalPanel;
    }

    private AccordionPanel createAndGetAggregateAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Aggregates");

        FormLayout aggregatesDialogFormLayout = new FormLayout();
        // specific
        Checkbox showAggregatesCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showAggregatesCheckbox, DiagramStylingConfiguration::isShowAggregates, DiagramStylingConfiguration::setShowAggregates);

        showAggregateFieldsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showAggregateFieldsCheckbox, DiagramStylingConfiguration::isShowAggregateFields, DiagramStylingConfiguration::setShowAggregateFields);

        showAggregateMethodsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showAggregateMethodsCheckbox, DiagramStylingConfiguration::isShowAggregateMethods, DiagramStylingConfiguration::setShowAggregateMethods);

        aggregatesDialogFormLayout.addFormItem(showAggregatesCheckbox,"Show");
        aggregatesDialogFormLayout.addFormItem(showAggregateFieldsCheckbox,"Fields");
        aggregatesDialogFormLayout.addFormItem(showAggregateMethodsCheckbox,"Methods");

        accordionPanel.add(aggregatesDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetDomainEventAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Domain Events");

        FormLayout domainEventDialogFormLayout = new FormLayout();
        // specific
        Checkbox showDomainEventsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showDomainEventsCheckbox, DiagramStylingConfiguration::isShowDomainEvents, DiagramStylingConfiguration::setShowDomainEvents);

        showDomainEventFieldsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showDomainEventFieldsCheckbox, DiagramStylingConfiguration::isShowDomainEventFields, DiagramStylingConfiguration::setShowDomainEventFields);

        showDomainEventMethodsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showDomainEventMethodsCheckbox, DiagramStylingConfiguration::isShowDomainEventMethods, DiagramStylingConfiguration::setShowDomainEventMethods);

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

        Checkbox showDomainCommandsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showDomainCommandsCheckbox, DiagramStylingConfiguration::isShowDomainCommands, DiagramStylingConfiguration::setShowDomainCommands);

        showDomainCommandFieldsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showDomainCommandFieldsCheckbox, DiagramStylingConfiguration::isShowDomainCommandFields, DiagramStylingConfiguration::setShowDomainCommandFields);

        showDomainCommandMethodsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showDomainCommandMethodsCheckbox, DiagramStylingConfiguration::isShowDomainCommandMethods, DiagramStylingConfiguration::setShowDomainCommandMethods);

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

        Checkbox showApplicationServicesCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showApplicationServicesCheckbox, DiagramStylingConfiguration::isShowApplicationServices, DiagramStylingConfiguration::setShowApplicationServices);

        showApplicationServiceFieldsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showApplicationServiceFieldsCheckbox, DiagramStylingConfiguration::isShowApplicationServiceFields, DiagramStylingConfiguration::setShowApplicationServiceFields);

        showApplicationServiceMethodsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showApplicationServiceMethodsCheckbox, DiagramStylingConfiguration::isShowApplicationServiceMethods, DiagramStylingConfiguration::setShowApplicationServiceMethods);

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

        Checkbox showDomainServicesCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showDomainServicesCheckbox, DiagramStylingConfiguration::isShowDomainServices, DiagramStylingConfiguration::setShowDomainServices);

        showDomainServiceFieldsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showDomainServiceFieldsCheckbox, DiagramStylingConfiguration::isShowDomainServiceFields, DiagramStylingConfiguration::setShowDomainServiceFields);

        showDomainServiceMethodsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showDomainServiceMethodsCheckbox, DiagramStylingConfiguration::isShowDomainServiceMethods, DiagramStylingConfiguration::setShowDomainServiceMethods);

        domainServiceDialogFormLayout.addFormItem(showDomainServicesCheckbox,"Show");
        domainServiceDialogFormLayout.addFormItem(showDomainServiceFieldsCheckbox,"Fields");
        domainServiceDialogFormLayout.addFormItem(showDomainServiceMethodsCheckbox,"Methods");

        accordionPanel.add(domainServiceDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetRepositoryAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Repository");

        FormLayout repositoryDialogFormLayout = new FormLayout();

        Checkbox showRepositoriesCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showRepositoriesCheckbox, DiagramStylingConfiguration::isShowRepositories, DiagramStylingConfiguration::setShowRepositories);

        showRepositoryFieldsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showRepositoryFieldsCheckbox, DiagramStylingConfiguration::isShowRepositoryFields, DiagramStylingConfiguration::setShowRepositoryFields);

        showRepositoryMethodsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showRepositoryMethodsCheckbox, DiagramStylingConfiguration::isShowRepositoryMethods, DiagramStylingConfiguration::setShowRepositoryMethods);

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

        Checkbox showReadModelsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showReadModelsCheckbox, DiagramStylingConfiguration::isShowReadModels, DiagramStylingConfiguration::setShowReadModels);

        showReadModelFieldsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showReadModelFieldsCheckbox, DiagramStylingConfiguration::isShowReadModelFields, DiagramStylingConfiguration::setShowReadModelFields);

        showReadModelMethodsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showReadModelMethodsCheckbox, DiagramStylingConfiguration::isShowReadModelMethods, DiagramStylingConfiguration::setShowReadModelMethods);

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

        Checkbox showQueryHandlersCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showQueryHandlersCheckbox, DiagramStylingConfiguration::isShowQueryHandlers, DiagramStylingConfiguration::setShowQueryHandlers);

        showQueryHandlerFieldsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showQueryHandlerFieldsCheckbox, DiagramStylingConfiguration::isShowQueryHandlerFields, DiagramStylingConfiguration::setShowQueryHandlerFields);

        showQueryHandlerMethodsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showQueryHandlerMethodsCheckbox, DiagramStylingConfiguration::isShowQueryHandlerMethods, DiagramStylingConfiguration::setShowQueryHandlerMethods);

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

        Checkbox showOutboundServicesCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showOutboundServicesCheckbox, DiagramStylingConfiguration::isShowOutboundServices, DiagramStylingConfiguration::setShowOutboundServices);

        showOutboundServiceFieldsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showOutboundServiceFieldsCheckbox, DiagramStylingConfiguration::isShowOutboundServiceFields, DiagramStylingConfiguration::setShowOutboundServiceFields);

        showOutboundServiceMethodsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showOutboundServiceMethodsCheckbox, DiagramStylingConfiguration::isShowOutboundServiceMethods, DiagramStylingConfiguration::setShowOutboundServiceMethods);

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

        Checkbox showUnspecifiedServiceKindsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showUnspecifiedServiceKindsCheckbox, DiagramStylingConfiguration::isShowUnspecifiedServiceKinds, DiagramStylingConfiguration::setShowUnspecifiedServiceKinds);

        showUnspecifiedServiceKindFieldsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showUnspecifiedServiceKindFieldsCheckbox, DiagramStylingConfiguration::isShowUnspecifiedServiceKindFields, DiagramStylingConfiguration::setShowUnspecifiedServiceKindFields);

        showUnspecifiedServiceKindMethodsCheckbox = new Checkbox();
        diagramConfigurationBinder.bind(showUnspecifiedServiceKindMethodsCheckbox, DiagramStylingConfiguration::isShowUnspecifiedServiceKindMethods, DiagramStylingConfiguration::setShowUnspecifiedServiceKindMethods);

        unspecifiedServiceKindDialogFormLayout.addFormItem(showUnspecifiedServiceKindsCheckbox,"Show");
        unspecifiedServiceKindDialogFormLayout.addFormItem(showUnspecifiedServiceKindFieldsCheckbox,"Fields");
        unspecifiedServiceKindDialogFormLayout.addFormItem(showUnspecifiedServiceKindMethodsCheckbox,"Methods");

        accordionPanel.add(unspecifiedServiceKindDialogFormLayout);
        return accordionPanel;
    }

    private void invertShowFieldsCheckboxes() {
        Boolean showAllFieldsCheckboxValue = showAllFieldsCheckbox.getValue();
        showDomainEventFieldsCheckbox.setEnabled(showAllFieldsCheckboxValue);
        showDomainCommandFieldsCheckbox.setEnabled(showAllFieldsCheckboxValue);
        showApplicationServiceFieldsCheckbox.setEnabled(showAllFieldsCheckboxValue);
        showDomainServiceFieldsCheckbox.setEnabled(showAllFieldsCheckboxValue);
        showRepositoryFieldsCheckbox.setEnabled(showAllFieldsCheckboxValue);
        showReadModelFieldsCheckbox.setEnabled(showAllFieldsCheckboxValue);
        showQueryHandlerFieldsCheckbox.setEnabled(showAllFieldsCheckboxValue);
        showOutboundServiceFieldsCheckbox.setEnabled(showAllFieldsCheckboxValue);
        showUnspecifiedServiceKindFieldsCheckbox.setEnabled(showAllFieldsCheckboxValue);
    }

    private void invertShowMethodsCheckboxes() {
        Boolean showAllMethodsCheckboxValue = showAllMethodsCheckbox.getValue();
        showDomainEventMethodsCheckbox.setEnabled(showAllMethodsCheckboxValue);
        showDomainCommandMethodsCheckbox.setEnabled(showAllMethodsCheckboxValue);
        showApplicationServiceMethodsCheckbox.setEnabled(showAllMethodsCheckboxValue);
        showDomainServiceMethodsCheckbox.setEnabled(showAllMethodsCheckboxValue);
        showRepositoryMethodsCheckbox.setEnabled(showAllMethodsCheckboxValue);
        showReadModelMethodsCheckbox.setEnabled(showAllMethodsCheckboxValue);
        showQueryHandlerMethodsCheckbox.setEnabled(showAllMethodsCheckboxValue);
        showOutboundServiceMethodsCheckbox.setEnabled(showAllMethodsCheckboxValue);
        showUnspecifiedServiceKindMethodsCheckbox.setEnabled(showAllMethodsCheckboxValue);
    }
}
