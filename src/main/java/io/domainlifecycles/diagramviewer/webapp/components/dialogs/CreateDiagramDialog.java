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
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.rest.kroki.FileType;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.components.various.PackageSelectChipField;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.util.List;
import java.util.Set;
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

    public CreateDiagramDialog(DiagramService diagramService, Project project, List<DomainTypeMirror> domainTypeMirrors) {
        this.diagramService = diagramService;
        this.project = project;
        this.domainTypeMirrors = domainTypeMirrors;
        this.binder = new Binder<>();

        this.createDiagramOptions = new CreateDiagramOptions(FileType.SVG);

        setHeaderTitle("Create Diagram");
        setWidth("50%");

        getFooter().add(createCreateButton());
        getFooter().add(createCancelButton());
        add(createDialogLayout());

        binder.readBean(createDiagramOptions);
        binder.addStatusChangeListener(event -> createButton.setEnabled(binder.isValid()));
    }

    private Button createCreateButton() {
        createButton = new Button("Create");

        createButton.addClickListener(e -> {
            binder.writeBeanIfValid(createDiagramOptions);
            diagramService.create(project, createDiagramOptions.getFileName(), createDiagramOptions.getFileType(), createDiagramOptions.getFilteredPackages(), createDiagramOptions.getBlacklistedClassnames());
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

    private Accordion createAndGetDialogAdvancedConfigurationAccordion() {
        Accordion advancedConfigurationAccordion = new Accordion();
        AccordionPanel advancedConfigurationPanel = new AccordionPanel("Advanced configuration");

        FormLayout advancedConfigurationFormLayout = new FormLayout();
        advancedConfigurationFormLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        Select<Diagram> diagramTemplateSelect = new Select<>();
        diagramTemplateSelect.setWidthFull();
        diagramTemplateSelect.setEmptySelectionAllowed(true);
        diagramTemplateSelect.setItems(diagramService.findAll(project.getId()));
        diagramTemplateSelect.setItemLabelGenerator(diagram -> diagram == null ? "" : diagram.getFileName());
        advancedConfigurationFormLayout.addFormItem(diagramTemplateSelect, "Template");

        PackageSelectChipField packageSelectChipField = new PackageSelectChipField();
        packageSelectChipField.setWidthFull();
        binder.forField(packageSelectChipField)
            .bind(CreateDiagramOptions::getFilteredPackages, CreateDiagramOptions::setFilteredPackages);
        advancedConfigurationFormLayout.addFormItem(packageSelectChipField, "Filtered packages");

        MultiSelectComboBox<String> blacklistedClassnamesMultiSelectComboBox = new MultiSelectComboBox<>();
        blacklistedClassnamesMultiSelectComboBox.setWidthFull();
        blacklistedClassnamesMultiSelectComboBox.setPlaceholder("Classnames...");
        blacklistedClassnamesMultiSelectComboBox.setItems(domainTypeMirrors.stream().map(DomainTypeMirror::getTypeName).collect(
            Collectors.toSet()));
        binder.forField(blacklistedClassnamesMultiSelectComboBox)
            .bind(CreateDiagramOptions::getBlacklistedClassnames, CreateDiagramOptions::setBlacklistedClassnames);
        advancedConfigurationFormLayout.addFormItem(blacklistedClassnamesMultiSelectComboBox, "Excluded classes");

        diagramTemplateSelect.addValueChangeListener(e -> {
            createDiagramOptions = CreateDiagramOptions.builder()
                .fileType(FileType.SVG)
                .fileName(createDiagramOptions.getFileName())
                .blacklistedClassnames(e.getValue() == null ? null :
                    e.getValue().getDomainModelVisibility().getBlacklistedClassNames())
                .filteredPackages(e.getValue()  == null ? null :
                    e.getValue().getDomainModelVisibility().getFilteredPackageNames())
                .build();

            binder.readBean(createDiagramOptions);
        });

        advancedConfigurationPanel.add(advancedConfigurationFormLayout);
        advancedConfigurationAccordion.add(advancedConfigurationPanel);
        return advancedConfigurationAccordion;
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

    @Data
    @Builder
    @AllArgsConstructor
    private static class CreateDiagramOptions {
        private String fileName;
        private FileType fileType;
        private Set<String> filteredPackages;
        private Set<String> blacklistedClassnames;

        public CreateDiagramOptions(FileType fileType) {
            this.fileType = fileType;
        }
    }
}
