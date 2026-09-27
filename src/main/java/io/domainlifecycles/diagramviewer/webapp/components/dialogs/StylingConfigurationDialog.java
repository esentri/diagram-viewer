/*
 *     ________  .__
 *     \______ \ |__|____     ________________    _____
 *      |    |  \|  \__  \   / ___\_  __ \__  \  /     \
 *      |    |   \  |/ __ \_/ /_/  >  | \// __ \|  Y Y  \
 *     /_______  /__(____  /\___  /|__|  (____  /__|_|  /
 *             \/        \//_____/            \/      \/
 *     ____   ____.__
 *     \   \ /   /|__| ______  _  __ ___________
 *      \   Y   / |  |/ __ \ \/ \/ // __ \_  __ \
 *       \     /  |  \  ___/\     /\  ___/|  | \/
 *        \___/   |__|\___  >\/\_/  \___  >__|
 *                        \/            \/
 *
 *  Copyright 2025-2026 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.Binder;
import io.domainlifecycles.diagramviewer.webapp.rendering.BackgroundDiagramRendering;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.components.ColorPickerComponent;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.Styling;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dialog allowing configuration for each DDD building block (i.e. AggregateRoot, Repository, etc.).
 *
 * @author leonvoellinger
 */
@Slf4j
public class StylingConfigurationDialog extends Dialog {


    private Diagram diagram = null;

    private final DiagramService diagramService;
    private final Binder<DiagramStylingConfiguration> binder;

    public StylingConfigurationDialog(DiagramService diagramService) {
        this.diagramService = diagramService;
        this.binder = new Binder<>(DiagramStylingConfiguration.class);

        setHeaderTitle("Configuration | Diagram node styles");

        setWidth("40%");
        setHeight("60%");

        add(createDialogLayout());

        addOpenedChangeListener(e -> {
            if(e.isOpened() && diagram != null) {
                binder.readBean(diagram.getDiagramStylingConfiguration());
            }
        });

        getFooter().add(createSaveButton());
        getFooter().add(createCancelButton());
    }

    public void setDiagram(Diagram diagram){
        log.debug("Setting new diagram");
        this.diagram = diagram;
        binder.readBean(diagram.getDiagramStylingConfiguration());
    }

    private Button createSaveButton() {
        Button saveButton = new Button("Save");

        saveButton.addClickListener(e -> {
            binder.writeBeanIfValid(diagram.getDiagramStylingConfiguration());
            diagram = BackgroundDiagramRendering.updateModelAndImage(this, diagramService, diagram);
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
        accordion.add(createAndGetNonDomainClassAccordionPanel());

        return accordion;
    }

    private AccordionPanel createAndGetAggregateRootAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Aggregate Root");

        FormLayout aggregateRootDialogFormLayout = new FormLayout();

        ColorPickerComponent aggregateRootColorInput = new ColorPickerComponent();
        binder.forField(aggregateRootColorInput)
            .bind(diagramStylingConfiguration -> extractColorConfiguration(diagramStylingConfiguration.getAggregateRootStyle()),
                (diagramStylingConfiguration, newColorHexString) -> diagramStylingConfiguration.setAggregateRootStyle(buildNewColorConfiguration(diagramStylingConfiguration.getAggregateRootStyle(), newColorHexString)));
        aggregateRootDialogFormLayout.addFormItem(aggregateRootColorInput, "Color");

        MultiSelectComboBox<Styling> aggregateRootStylingOptionsSelect = new MultiSelectComboBox<>();
        binder.forField(aggregateRootStylingOptionsSelect)
                .bind(diagramStylingConfiguration -> Styling.map(extractStylingConfiguration(diagramStylingConfiguration.getAggregateRootStyle())),
                    (diagramStylingConfiguration, selectedStylings) -> diagramStylingConfiguration.setAggregateRootStyle(buildNewStylingConfiguration(
                        diagramStylingConfiguration.getAggregateRootStyle(), selectedStylings)));
        aggregateRootStylingOptionsSelect.setItems(Styling.values());
        aggregateRootStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        aggregateRootDialogFormLayout.addFormItem(aggregateRootStylingOptionsSelect, "Styling Options");

        accordionPanel.add(aggregateRootDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetEntityAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Entity");

        FormLayout entityDialogFormLayout = new FormLayout();

        ColorPickerComponent entityColorInput = new ColorPickerComponent();
        binder.forField(entityColorInput)
            .bind(diagramStylingConfiguration -> extractColorConfiguration(diagramStylingConfiguration.getEntityStyle()),
                (diagramStylingConfiguration, newColorHexString) -> diagramStylingConfiguration.setEntityStyle(buildNewColorConfiguration(diagramStylingConfiguration.getEntityStyle(), newColorHexString)));
        entityDialogFormLayout.addFormItem(entityColorInput, "Color");

        MultiSelectComboBox<Styling> entityStylingOptionsSelect = new MultiSelectComboBox<>();
        binder.forField(entityStylingOptionsSelect)
            .bind(diagramStylingConfiguration -> Styling.map(extractStylingConfiguration(diagramStylingConfiguration.getEntityStyle())),
                (diagramStylingConfiguration, selectedStylings) -> diagramStylingConfiguration.setEntityStyle(buildNewStylingConfiguration(
                    diagramStylingConfiguration.getEntityStyle(), selectedStylings)));
        entityStylingOptionsSelect.setItems(Styling.values());
        entityStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        entityDialogFormLayout.addFormItem(entityStylingOptionsSelect, "Styling Options");

        accordionPanel.add(entityDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetValueObjectAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Value Object");

        FormLayout valueObjectDialogFormLayout = new FormLayout();

        ColorPickerComponent valueObjectColorInput = new ColorPickerComponent();
        binder.forField(valueObjectColorInput)
            .bind(diagramStylingConfiguration -> extractColorConfiguration(diagramStylingConfiguration.getValueObjectStyle()),
                (diagramStylingConfiguration, newColorHexString) -> diagramStylingConfiguration.setValueObjectStyle(buildNewColorConfiguration(diagramStylingConfiguration.getValueObjectStyle(), newColorHexString)));
        valueObjectDialogFormLayout.addFormItem(valueObjectColorInput, "Color");

        MultiSelectComboBox<Styling> valueObjectStylingOptionsSelect = new MultiSelectComboBox<>();
        binder.forField(valueObjectStylingOptionsSelect)
            .bind(diagramStylingConfiguration -> Styling.map(extractStylingConfiguration(diagramStylingConfiguration.getValueObjectStyle())),
                (diagramStylingConfiguration, selectedStylings) -> diagramStylingConfiguration.setValueObjectStyle(buildNewStylingConfiguration(
                    diagramStylingConfiguration.getValueObjectStyle(), selectedStylings)));
        valueObjectStylingOptionsSelect.setItems(Styling.values());
        valueObjectStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        valueObjectDialogFormLayout.addFormItem(valueObjectStylingOptionsSelect, "Styling Options");

        accordionPanel.add(valueObjectDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetEnumAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Enum");

        FormLayout enumDialogFormLayout = new FormLayout();

        ColorPickerComponent enumColorInput = new ColorPickerComponent();
        binder.forField(enumColorInput)
            .bind(diagramStylingConfiguration -> extractColorConfiguration(diagramStylingConfiguration.getEnumStyle()),
                (diagramStylingConfiguration, newColorHexString) -> diagramStylingConfiguration.setEnumStyle(buildNewColorConfiguration(diagramStylingConfiguration.getEnumStyle(), newColorHexString)));
        enumDialogFormLayout.addFormItem(enumColorInput, "Color");

        MultiSelectComboBox<Styling> enumStylingOptionsSelect = new MultiSelectComboBox<>();
        binder.forField(enumStylingOptionsSelect)
            .bind(diagramStylingConfiguration -> Styling.map(extractStylingConfiguration(diagramStylingConfiguration.getEnumStyle())),
                (diagramStylingConfiguration, selectedStylings) -> diagramStylingConfiguration.setEnumStyle(buildNewStylingConfiguration(
                    diagramStylingConfiguration.getEnumStyle(), selectedStylings)));
        enumStylingOptionsSelect.setItems(Styling.values());
        enumStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        enumDialogFormLayout.addFormItem(enumStylingOptionsSelect, "Styling Options");

        accordionPanel.add(enumDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetIdentityAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Identity");

        FormLayout identityDialogFormLayout = new FormLayout();

        ColorPickerComponent identityColorInput = new ColorPickerComponent();
        binder.forField(identityColorInput)
            .bind(diagramStylingConfiguration -> extractColorConfiguration(diagramStylingConfiguration.getIdentityStyle()),
                (diagramStylingConfiguration, newColorHexString) -> diagramStylingConfiguration.setIdentityStyle(buildNewColorConfiguration(diagramStylingConfiguration.getIdentityStyle(), newColorHexString)));
        identityDialogFormLayout.addFormItem(identityColorInput, "Color");

        MultiSelectComboBox<Styling> identityStylingOptionsSelect = new MultiSelectComboBox<>();
        binder.forField(identityStylingOptionsSelect)
            .bind(diagramStylingConfiguration -> Styling.map(extractStylingConfiguration(diagramStylingConfiguration.getIdentityStyle())),
                (diagramStylingConfiguration, selectedStylings) -> diagramStylingConfiguration.setIdentityStyle(buildNewStylingConfiguration(
                    diagramStylingConfiguration.getIdentityStyle(), selectedStylings)));
        identityStylingOptionsSelect.setItems(Styling.values());
        identityStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        identityDialogFormLayout.addFormItem(identityStylingOptionsSelect, "Styling Options");

        accordionPanel.add(identityDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetDomainEventAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Domain Event");

        FormLayout domainEventDialogFormLayout = new FormLayout();

        ColorPickerComponent domainEventColorInput = new ColorPickerComponent();
        binder.forField(domainEventColorInput)
            .bind(diagramStylingConfiguration -> extractColorConfiguration(diagramStylingConfiguration.getDomainEventStyle()),
                (diagramStylingConfiguration, newColorHexString) -> diagramStylingConfiguration.setDomainEventStyle(buildNewColorConfiguration(diagramStylingConfiguration.getDomainEventStyle(), newColorHexString)));
        domainEventDialogFormLayout.addFormItem(domainEventColorInput, "Color");

        MultiSelectComboBox<Styling> domainEventStylingOptionsSelect = new MultiSelectComboBox<>();
        binder.forField(domainEventStylingOptionsSelect)
            .bind(diagramStylingConfiguration -> Styling.map(extractStylingConfiguration(diagramStylingConfiguration.getDomainEventStyle())),
                (diagramStylingConfiguration, selectedStylings) -> diagramStylingConfiguration.setDomainEventStyle(buildNewStylingConfiguration(
                    diagramStylingConfiguration.getDomainEventStyle(), selectedStylings)));
        domainEventStylingOptionsSelect.setItems(Styling.values());
        domainEventStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        domainEventDialogFormLayout.addFormItem(domainEventStylingOptionsSelect, "Styling Options");

        accordionPanel.add(domainEventDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetDomainCommandAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Domain Command");

        FormLayout domainCommandDialogFormLayout = new FormLayout();

        ColorPickerComponent domainCommandColorInput = new ColorPickerComponent();
        binder.forField(domainCommandColorInput)
            .bind(diagramStylingConfiguration -> extractColorConfiguration(diagramStylingConfiguration.getDomainCommandStyle()),
                (diagramStylingConfiguration, newColorHexString) -> diagramStylingConfiguration.setDomainCommandStyle(buildNewColorConfiguration(diagramStylingConfiguration.getDomainCommandStyle(), newColorHexString)));
        domainCommandDialogFormLayout.addFormItem(domainCommandColorInput, "Color");

        MultiSelectComboBox<Styling> domainCommandStylingOptionsSelect = new MultiSelectComboBox<>();
        binder.forField(domainCommandStylingOptionsSelect)
            .bind(diagramStylingConfiguration -> Styling.map(extractStylingConfiguration(diagramStylingConfiguration.getDomainCommandStyle())),
                (diagramStylingConfiguration, selectedStylings) -> diagramStylingConfiguration.setDomainCommandStyle(buildNewStylingConfiguration(
                    diagramStylingConfiguration.getDomainCommandStyle(), selectedStylings)));
        domainCommandStylingOptionsSelect.setItems(Styling.values());
        domainCommandStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        domainCommandDialogFormLayout.addFormItem(domainCommandStylingOptionsSelect, "Styling Options");

        accordionPanel.add(domainCommandDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetApplicationServiceAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Application Service");

        FormLayout applicationServiceDialogFormLayout = new FormLayout();

        ColorPickerComponent applicationServiceColorInput = new ColorPickerComponent();
        binder.forField(applicationServiceColorInput)
            .bind(diagramStylingConfiguration -> extractColorConfiguration(diagramStylingConfiguration.getApplicationServiceStyle()),
                (diagramStylingConfiguration, newColorHexString) -> diagramStylingConfiguration.setApplicationServiceStyle(buildNewColorConfiguration(diagramStylingConfiguration.getApplicationServiceStyle(), newColorHexString)));
        applicationServiceDialogFormLayout.addFormItem(applicationServiceColorInput, "Color");

        MultiSelectComboBox<Styling> applicationServiceStylingOptionsSelect = new MultiSelectComboBox<>();
        binder.forField(applicationServiceStylingOptionsSelect)
            .bind(diagramStylingConfiguration -> Styling.map(extractStylingConfiguration(diagramStylingConfiguration.getApplicationServiceStyle())),
                (diagramStylingConfiguration, selectedStylings) -> diagramStylingConfiguration.setApplicationServiceStyle(buildNewStylingConfiguration(
                    diagramStylingConfiguration.getApplicationServiceStyle(), selectedStylings)));
        applicationServiceStylingOptionsSelect.setItems(Styling.values());
        applicationServiceStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        applicationServiceDialogFormLayout.addFormItem(applicationServiceStylingOptionsSelect, "Styling Options");

        accordionPanel.add(applicationServiceDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetDomainServiceAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Domain Service");

        FormLayout domainServiceDialogFormLayout = new FormLayout();

        ColorPickerComponent domainServiceColorInput = new ColorPickerComponent();
        binder.forField(domainServiceColorInput)
            .bind(diagramStylingConfiguration -> extractColorConfiguration(diagramStylingConfiguration.getDomainServiceStyle()),
                (diagramStylingConfiguration, newColorHexString) -> diagramStylingConfiguration.setDomainServiceStyle(buildNewColorConfiguration(diagramStylingConfiguration.getDomainServiceStyle(), newColorHexString)));
        domainServiceDialogFormLayout.addFormItem(domainServiceColorInput, "Color");

        MultiSelectComboBox<Styling> domainServiceStylingOptionsSelect = new MultiSelectComboBox<>();
        binder.forField(domainServiceStylingOptionsSelect)
            .bind(diagramStylingConfiguration -> Styling.map(extractStylingConfiguration(diagramStylingConfiguration.getDomainServiceStyle())),
                (diagramStylingConfiguration, selectedStylings) -> diagramStylingConfiguration.setDomainServiceStyle(buildNewStylingConfiguration(
                    diagramStylingConfiguration.getDomainServiceStyle(), selectedStylings)));
        domainServiceStylingOptionsSelect.setItems(Styling.values());
        domainServiceStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        domainServiceDialogFormLayout.addFormItem(domainServiceStylingOptionsSelect, "Styling Options");

        accordionPanel.add(domainServiceDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetRepositoryAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Repository");

        FormLayout repositoryDialogFormLayout = new FormLayout();

        ColorPickerComponent repositoryColorInput = new ColorPickerComponent();
        binder.forField(repositoryColorInput)
            .bind(diagramStylingConfiguration -> extractColorConfiguration(diagramStylingConfiguration.getRepositoryStyle()),
                (diagramStylingConfiguration, newColorHexString) -> diagramStylingConfiguration.setRepositoryStyle(buildNewColorConfiguration(diagramStylingConfiguration.getRepositoryStyle(), newColorHexString)));
        repositoryDialogFormLayout.addFormItem(repositoryColorInput, "Color");

        MultiSelectComboBox<Styling> repositoryStylingOptionsSelect = new MultiSelectComboBox<>();
        binder.forField(repositoryStylingOptionsSelect)
            .bind(diagramStylingConfiguration -> Styling.map(extractStylingConfiguration(diagramStylingConfiguration.getRepositoryStyle())),
                (diagramStylingConfiguration, selectedStylings) -> diagramStylingConfiguration.setRepositoryStyle(buildNewStylingConfiguration(
                    diagramStylingConfiguration.getRepositoryStyle(), selectedStylings)));
        repositoryStylingOptionsSelect.setItems(Styling.values());
        repositoryStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        repositoryDialogFormLayout.addFormItem(repositoryStylingOptionsSelect, "Styling Options");

        accordionPanel.add(repositoryDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetReadModelAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Read Model");

        FormLayout readModelDialogFormLayout = new FormLayout();

        ColorPickerComponent readModelColorInput = new ColorPickerComponent();
        binder.forField(readModelColorInput)
            .bind(diagramStylingConfiguration -> extractColorConfiguration(diagramStylingConfiguration.getReadModelStyle()),
                (diagramStylingConfiguration, newColorHexString) -> diagramStylingConfiguration.setReadModelStyle(buildNewColorConfiguration(diagramStylingConfiguration.getReadModelStyle(), newColorHexString)));
        readModelDialogFormLayout.addFormItem(readModelColorInput, "Color");

        MultiSelectComboBox<Styling> readModelStylingOptionsSelect = new MultiSelectComboBox<>();
        binder.forField(readModelStylingOptionsSelect)
            .bind(diagramStylingConfiguration -> Styling.map(extractStylingConfiguration(diagramStylingConfiguration.getReadModelStyle())),
                (diagramStylingConfiguration, selectedStylings) -> diagramStylingConfiguration.setReadModelStyle(buildNewStylingConfiguration(
                    diagramStylingConfiguration.getReadModelStyle(), selectedStylings)));
        readModelStylingOptionsSelect.setItems(Styling.values());
        readModelStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        readModelDialogFormLayout.addFormItem(readModelStylingOptionsSelect, "Styling Options");

        accordionPanel.add(readModelDialogFormLayout);
        return accordionPanel;
    }
    private AccordionPanel createAndGetQueryHandlerAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Query Handler");

        FormLayout queryHandlerDialogFormLayout = new FormLayout();

        ColorPickerComponent queryHandlerColorInput = new ColorPickerComponent();
        binder.forField(queryHandlerColorInput)
            .bind(diagramStylingConfiguration -> extractColorConfiguration(diagramStylingConfiguration.getQueryHandlerStyle()),
                (diagramStylingConfiguration, newColorHexString) -> diagramStylingConfiguration.setQueryHandlerStyle(buildNewColorConfiguration(diagramStylingConfiguration.getQueryHandlerStyle(), newColorHexString)));
        queryHandlerDialogFormLayout.addFormItem(queryHandlerColorInput, "Color");

        MultiSelectComboBox<Styling> queryHandlerStylingOptionsSelect = new MultiSelectComboBox<>();
        binder.forField(queryHandlerStylingOptionsSelect)
            .bind(diagramStylingConfiguration -> Styling.map(extractStylingConfiguration(diagramStylingConfiguration.getQueryHandlerStyle())),
                (diagramStylingConfiguration, selectedStylings) -> diagramStylingConfiguration.setQueryHandlerStyle(buildNewStylingConfiguration(
                    diagramStylingConfiguration.getQueryHandlerStyle(), selectedStylings)));
        queryHandlerStylingOptionsSelect.setItems(Styling.values());
        queryHandlerStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        queryHandlerDialogFormLayout.addFormItem(queryHandlerStylingOptionsSelect, "Styling Options");

        accordionPanel.add(queryHandlerDialogFormLayout);
        return accordionPanel;
    }
    private AccordionPanel createAndGetOutboundServiceAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Outbound Service");

        FormLayout outboundServiceDialogFormLayout = new FormLayout();

        ColorPickerComponent outboundServiceColorInput = new ColorPickerComponent();
        binder.forField(outboundServiceColorInput)
            .bind(diagramStylingConfiguration -> extractColorConfiguration(diagramStylingConfiguration.getOutboundServiceStyle()),
                (diagramStylingConfiguration, newColorHexString) -> diagramStylingConfiguration.setOutboundServiceStyle(buildNewColorConfiguration(diagramStylingConfiguration.getOutboundServiceStyle(), newColorHexString)));
        outboundServiceDialogFormLayout.addFormItem(outboundServiceColorInput, "Color");

        MultiSelectComboBox<Styling> outboundServiceStylingOptionsSelect = new MultiSelectComboBox<>();
        binder.forField(outboundServiceStylingOptionsSelect)
            .bind(diagramStylingConfiguration -> Styling.map(extractStylingConfiguration(diagramStylingConfiguration.getOutboundServiceStyle())),
                (diagramStylingConfiguration, selectedStylings) -> diagramStylingConfiguration.setOutboundServiceStyle(buildNewStylingConfiguration(
                    diagramStylingConfiguration.getOutboundServiceStyle(), selectedStylings)));
        outboundServiceStylingOptionsSelect.setItems(Styling.values());
        outboundServiceStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        outboundServiceDialogFormLayout.addFormItem(outboundServiceStylingOptionsSelect, "Styling Options");

        accordionPanel.add(outboundServiceDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetUnspecifiedServiceKindAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Service Kind");

        FormLayout unspecifiedServiceKindDialogFormLayout = new FormLayout();

        ColorPickerComponent unspecifiedServiceKindColorInput = new ColorPickerComponent();
        binder.forField(unspecifiedServiceKindColorInput)
            .bind(diagramStylingConfiguration -> extractColorConfiguration(diagramStylingConfiguration.getUnspecifiedServiceKindStyle()),
                (diagramStylingConfiguration, newColorHexString) -> diagramStylingConfiguration.setUnspecifiedServiceKindStyle(buildNewColorConfiguration(diagramStylingConfiguration.getUnspecifiedServiceKindStyle(), newColorHexString)));
        unspecifiedServiceKindDialogFormLayout.addFormItem(unspecifiedServiceKindColorInput, "Color");

        MultiSelectComboBox<Styling> unspecifiedServiceKindStylingOptionsSelect = new MultiSelectComboBox<>();
        binder.forField(unspecifiedServiceKindStylingOptionsSelect)
            .bind(diagramStylingConfiguration -> Styling.map(extractStylingConfiguration(diagramStylingConfiguration.getUnspecifiedServiceKindStyle())),
                (diagramStylingConfiguration, selectedStylings) -> diagramStylingConfiguration.setUnspecifiedServiceKindStyle(buildNewStylingConfiguration(
                    diagramStylingConfiguration.getUnspecifiedServiceKindStyle(), selectedStylings)));
        unspecifiedServiceKindStylingOptionsSelect.setItems(Styling.values());
        unspecifiedServiceKindStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        unspecifiedServiceKindDialogFormLayout.addFormItem(unspecifiedServiceKindStylingOptionsSelect, "Styling Options");

        accordionPanel.add(unspecifiedServiceKindDialogFormLayout);
        return accordionPanel;
    }

    private AccordionPanel createAndGetNonDomainClassAccordionPanel() {
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Non-Domain Class");

        FormLayout nonDomainClassDialogFormLayout = new FormLayout();

        ColorPickerComponent nonDomainClassColorInput = new ColorPickerComponent();
        binder.forField(nonDomainClassColorInput)
            .bind(diagramStylingConfiguration -> extractColorConfiguration(diagramStylingConfiguration.getNonDomainClassStyle()),
                (diagramStylingConfiguration, newColorHexString) -> diagramStylingConfiguration.setNonDomainClassStyle(buildNewColorConfiguration(diagramStylingConfiguration.getNonDomainClassStyle(), newColorHexString)));
        nonDomainClassDialogFormLayout.addFormItem(nonDomainClassColorInput, "Color");

        MultiSelectComboBox<Styling> nonDomainClassStylingOptionsSelect = new MultiSelectComboBox<>();
        binder.forField(nonDomainClassStylingOptionsSelect)
            .bind(diagramStylingConfiguration -> Styling.map(extractStylingConfiguration(diagramStylingConfiguration.getNonDomainClassStyle())),
                (diagramStylingConfiguration, selectedStylings) -> diagramStylingConfiguration.setNonDomainClassStyle(buildNewStylingConfiguration(
                    diagramStylingConfiguration.getNonDomainClassStyle(), selectedStylings)));
        nonDomainClassStylingOptionsSelect.setItems(Styling.values());
        nonDomainClassStylingOptionsSelect.setItemLabelGenerator(Styling::getDisplayValue);
        nonDomainClassDialogFormLayout.addFormItem(nonDomainClassStylingOptionsSelect, "Styling Options");

        accordionPanel.add(nonDomainClassDialogFormLayout);
        return accordionPanel;
    }

    /**
     * Extracts the styling part of the configuration.
     * @param configuration the current configuration, i.e. "fill=#FFFFCC bold italic"
     * @return an array containing the values "bold" and "italic"
     */
    private String[] extractStylingConfiguration(String configuration) {
        Pattern stylePattern = Pattern.compile("(?<=fill=[A-Fa-f0-9#]{7})(.*)");

        Matcher matcher = stylePattern.matcher(configuration);
        if (matcher.find()) {
            String styles = matcher.group().trim();
            // a color-only configuration (e.g. "fill=#EAEAEA") has no styling part at all
            return styles.isEmpty() ? new String[0] : styles.split("\\s+");
        } else {
            return new String[0];
        }
    }

    /**
     * Replaces the old color in the configuration String by the new picked color.
     * @param oldConfiguration the old configuration String, i.e. "fill=#FFFFCC bold"
     * @param newColorHexString the picked color in hex format, i.e. "#AA88EE"
     * @return the styling configuration with the new color, i.e. "fill=#AA88EE bold"
     */
    private String buildNewColorConfiguration(String oldConfiguration, String newColorHexString) {
        Pattern fillPattern = Pattern.compile("fill=#([A-Fa-f0-9]{6})");

        Matcher matcher = fillPattern.matcher(oldConfiguration);
        if (matcher.find()) {
            return oldConfiguration.replaceFirst(matcher.group(), "fill=" + newColorHexString);
        } else {
            return oldConfiguration;
        }
    }

    /**
     * Extracts the color from a styling configuration.
     * @param configuration the current configuration, i.e. "fill=#FFFFCC bold"
     * @return the color part, i.e. "#FFFFCC"
     */
    private String extractColorConfiguration(String configuration) {
        Pattern fillPattern = Pattern.compile("#([A-Fa-f0-9]{6})");
        Matcher matcher = fillPattern.matcher(configuration);

        if (matcher.find()) {
            return matcher.group();
        } else {
            return "";
        }
    }

    /**
     * Builds the new styling configuration after certain styled have been picked in the UI.
     * @param oldConfiguration the old configuration, i.e. "fill=#FFFFCC bold"
     * @param selectedStylingOptions Set of the selected Stylings, i.e. a Set containing BOLD, ITALIC, UNDERLINE
     * @return the newly assembled configuration String, i.e. "fill=#FFFFCC bold italic underline"
     */
    private String buildNewStylingConfiguration(String oldConfiguration, Set<Styling> selectedStylingOptions) {
        Pattern colorPattern = Pattern.compile("fill=#[A-Fa-f0-9]{6}");
        Matcher colorMatcher = colorPattern.matcher(oldConfiguration);

        String colorPart = "";
        if (colorMatcher.find()) {
            colorPart = colorMatcher.group();
        }

        StringBuilder newConfiguration = new StringBuilder(colorPart);

        for (Styling style : selectedStylingOptions) {
            newConfiguration.append(" ").append(style.getNomnomlValue());
        }

        return newConfiguration.toString();
    }

}
