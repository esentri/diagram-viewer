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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.rest.kroki.FileType;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.components.various.selects.PackageMultiSelectComboBox;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class CreateDiagramDialog extends Dialog {

    private final DiagramService diagramService;
    private final Project project;
    private final Binder<CreateDiagramOptions> binder;
    private final List<DomainTypeMirror> domainTypeMirrors;

    private CreateDiagramOptions createDiagramOptions;
    private Button createButton;
    private Select<Diagram> diagramTemplateSelect;
    private PackageMultiSelectComboBox packageMultiSelectComboBox;
    private MultiSelectComboBox<String> blacklistedClassnamesMultiSelectComboBox;

    public CreateDiagramDialog(DiagramService diagramService, Project project, List<DomainTypeMirror> domainTypeMirrors) {
        this.diagramService = diagramService;
        this.project = project;
        this.domainTypeMirrors = domainTypeMirrors;
        this.binder = new Binder<>();

        setHeaderTitle("Create Diagram");
        setWidth("50%");

        getFooter().add(createCreateButton());
        getFooter().add(createCancelButton());
        add(createDialogLayout());

        addOpenedChangeListener(e -> {
            if(e.isOpened()) {
                this.createDiagramOptions = new CreateDiagramOptions(FileType.SVG);
                binder.readBean(createDiagramOptions);
            }
        });

        binder.addStatusChangeListener(event -> createButton.setEnabled(binder.isValid()));
    }

    private Button createCreateButton() {
        createButton = new Button("Create");

        createButton.addClickListener(e -> {
            binder.writeBeanIfValid(createDiagramOptions);
            diagramService.create(
                    project,
                    createDiagramOptions.getFileName(),
                    createDiagramOptions.getFileType(),
                    createDiagramOptions.getDomainModelVisibility(),
                    createDiagramOptions.getDiagramStylingConfiguration());
            ComponentUtil.fireEvent(UI.getCurrent(), new DiagramsOrProjectsChangedEvent(this, false));
            close();
        });

        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return createButton;
    }

    private Button createCancelButton() {
        return new Button("Cancel", e -> close());
    }

    private VerticalLayout createDialogLayout() {
        VerticalLayout dialogLayout = new VerticalLayout();

        FormLayout formLayout = createAndGetDialogFormLayout();
        Accordion advancedConfigurationAccordion = createAndGetDialogAdvancedConfigurationAccordion();

        dialogLayout.add(formLayout, advancedConfigurationAccordion);
        return dialogLayout;
    }

    private FormLayout createAndGetDialogFormLayout() {
        FormLayout formLayout = new FormLayout();

        TextField diagramNameTextField = new TextField();
        binder.forField(diagramNameTextField)
            .asRequired("Name is required.")
            .bind(CreateDiagramOptions::getFileName, CreateDiagramOptions::setFileName);
        formLayout.addFormItem(diagramNameTextField, "File-Name");

        Select<FileType> formatSelect = new Select<>();
        formatSelect.setItems(FileType.values());
        formatSelect.setItemEnabledProvider(item -> item.equals(FileType.SVG));
        binder.forField(formatSelect)
            .asRequired("Format is required.")
            .bind(CreateDiagramOptions::getFileType, CreateDiagramOptions::setFileType);
        formLayout.addFormItem(formatSelect, "Format");

        return formLayout;
    }

    private Accordion createAndGetDialogAdvancedConfigurationAccordion() {
        Accordion advancedConfigurationAccordion = new Accordion();
        AccordionPanel advancedConfigurationPanel = new AccordionPanel("Advanced configuration");

        FormLayout advancedConfigurationFormLayout = new FormLayout();
        advancedConfigurationFormLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        createAndAddDiagramTemplateSelect(advancedConfigurationFormLayout);
        createAndAddPackageMultiSelectComboBox(advancedConfigurationFormLayout);
        createAndAddBlacklistedClassnamesMultiSelectComboBox(advancedConfigurationFormLayout);

        advancedConfigurationPanel.add(advancedConfigurationFormLayout);
        advancedConfigurationAccordion.add(advancedConfigurationPanel);
        return advancedConfigurationAccordion;
    }

    private void createAndAddDiagramTemplateSelect(FormLayout advancedConfigurationFormLayout) {
        diagramTemplateSelect = new Select<>();
        diagramTemplateSelect.setWidthFull();
        diagramTemplateSelect.setEmptySelectionAllowed(true);
        diagramTemplateSelect.setItems(project.getDiagrams());
        diagramTemplateSelect.setItemLabelGenerator(diagram -> diagram == null ? "" : diagram.getFileName());

        diagramTemplateSelect.addValueChangeListener(e -> {
            Diagram templateDiagram = e.getValue();

            DomainModelVisibility visibility = new DomainModelVisibility();
            DiagramStylingConfiguration diagramStyling = new DiagramStylingConfiguration();

            boolean templateDiagramSelected = templateDiagram != null;
            if(templateDiagramSelected) {

                // Use new visibility/styling instances but map values
                visibility = templateDiagram.getDomainModelVisibility().toBuilder()
                    .id(null)
                    .createdAt(null)
                    .changedAt(null)
                    .build();
                diagramStyling = templateDiagram.getDiagramStylingConfiguration().toBuilder()
                    .id(null)
                    .createdAt(null)
                    .changedAt(null)
                    .build();
            }
            advancedConfigurationFormLayout.addFormItem(diagramTemplateSelect, "Template");

            blacklistedClassnamesMultiSelectComboBox.setEnabled(!templateDiagramSelected);
            packageMultiSelectComboBox.setEnabled(!templateDiagramSelected);

            createDiagramOptions.setDomainModelVisibility(visibility);
            createDiagramOptions.setDiagramStylingConfiguration(diagramStyling);

            binder.readBean(createDiagramOptions);
        });
    }

    private void createAndAddPackageMultiSelectComboBox(FormLayout advancedConfigurationFormLayout) {
        packageMultiSelectComboBox = new PackageMultiSelectComboBox(domainTypeMirrors);
        packageMultiSelectComboBox.setWidthFull();
        binder.forField(packageMultiSelectComboBox)
            .bind(opt -> opt.getDomainModelVisibility().getExplicitlyIncludedPackagesNames(),
                    (opt, v) -> opt.setDomainModelVisibility(opt.getDomainModelVisibility().replaceExplicitlyIncludedPackagesNames(v)));
        advancedConfigurationFormLayout.addFormItem(packageMultiSelectComboBox, "Explicitly included packages");
    }

    private void createAndAddBlacklistedClassnamesMultiSelectComboBox(FormLayout advancedConfigurationFormLayout) {
        blacklistedClassnamesMultiSelectComboBox = new MultiSelectComboBox<>();
        blacklistedClassnamesMultiSelectComboBox.setWidthFull();
        blacklistedClassnamesMultiSelectComboBox.setPlaceholder("Classnames...");
        blacklistedClassnamesMultiSelectComboBox.setItems(domainTypeMirrors.stream().map(DomainTypeMirror::getTypeName).collect(
            Collectors.toSet()));
        binder.forField(blacklistedClassnamesMultiSelectComboBox)
            .bind(opt -> opt.getDomainModelVisibility().getBlacklistedClassNames(),
                    (opt, v) -> opt.setDomainModelVisibility(opt.getDomainModelVisibility().replaceBlacklistedClassNames(v)));
        advancedConfigurationFormLayout.addFormItem(blacklistedClassnamesMultiSelectComboBox, "Excluded classes");
    }

    @Data
    @Builder
    @AllArgsConstructor
    private static class CreateDiagramOptions {
        private String fileName;
        private FileType fileType;
        private DomainModelVisibility domainModelVisibility;
        private DiagramStylingConfiguration diagramStylingConfiguration;

        public CreateDiagramOptions(FileType fileType) {
            this.fileType = fileType;
            this.domainModelVisibility = new DomainModelVisibility();
            this.diagramStylingConfiguration = new DiagramStylingConfiguration();
        }
    }
}
