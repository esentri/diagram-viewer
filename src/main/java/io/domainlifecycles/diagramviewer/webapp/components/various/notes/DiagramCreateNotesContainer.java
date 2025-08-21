package io.domainlifecycles.diagramviewer.webapp.components.various.notes;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep.LabelsPosition;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramTypeNote;
import io.domainlifecycles.diagramviewer.service.DiagramTypeNoteService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramTypeNotesChangedEvent;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class DiagramCreateNotesContainer extends VerticalLayout {

    private final static double STRING_LENGTH_TO_REM_FACTOR = 0.6;

    private final List<DomainTypeMirror> domainTypeMirrors;
    private final Diagram diagram;
    private final DiagramTypeNoteService diagramTypeNoteService;
    private final Binder<TypeNotes> binder;

    private TypeNotes typeNotes;
    private Button saveButton;

    public DiagramCreateNotesContainer(List<DomainTypeMirror> domainTypeMirrors, DiagramTypeNoteService diagramTypeNoteService, Diagram diagram) {
        this.domainTypeMirrors = domainTypeMirrors;
        this.diagram = diagram;
        this.diagramTypeNoteService = diagramTypeNoteService;
        this.binder = new Binder<>();

        setPadding(false);
        setMargin(false);

        add(getButtonLayout(), createFormLayout());
        binder.addStatusChangeListener(event -> saveButton.setEnabled(binder.isValid()));
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1, LabelsPosition.TOP));
        formLayout.setSizeFull();

        TextArea typeNotesTextArea = new TextArea();
        typeNotesTextArea.setWidthFull();
        typeNotesTextArea.setHeight("27rem");
        typeNotesTextArea.setMaxLength(DiagramTypeNote.NOTES_MAX_LENGTH);
        typeNotesTextArea.setClearButtonVisible(true);
        typeNotesTextArea.setEnabled(false);
        typeNotesTextArea.addValueChangeListener(e -> e.getSource()
            .setHelperText(e.getValue().length() + "/" + DiagramTypeNote.NOTES_MAX_LENGTH));
        typeNotesTextArea.setValueChangeMode(ValueChangeMode.EAGER);
        binder.forField(typeNotesTextArea)
            .bind(TypeNotes::getNotes, TypeNotes::setNotes);

        Set<DomainTypeMirror> allDomainTypeMirrorsInIncludedPackages = getAllDomainTypeMirrorsInIncludedPackages(domainTypeMirrors, diagram.getDomainModelVisibility().getExplicitlyIncludedPackagesNames());
        int longestDomainTypeMirrorNameLength = allDomainTypeMirrorsInIncludedPackages.stream()
            .map(DomainTypeMirror::getTypeName)
            .max(Comparator.comparingInt(String::length))
            .orElse("").length();

        ComboBox<DomainTypeMirror> domainTypeComboBox = new ComboBox<>();
        domainTypeComboBox.setItems(allDomainTypeMirrorsInIncludedPackages);
        domainTypeComboBox.setItemLabelGenerator(DomainTypeMirror::getTypeName);
        domainTypeComboBox.setWidthFull();
        getStyle().set("--vaadin-combo-box-overlay-width", longestDomainTypeMirrorNameLength * STRING_LENGTH_TO_REM_FACTOR + "rem");
        domainTypeComboBox.addValueChangeListener(e -> {
            DomainTypeMirror selectedDomainTypeMirror = e.getValue();
            String notes = diagramTypeNoteService.getNotes(diagram, selectedDomainTypeMirror);
            typeNotes = new TypeNotes(notes, selectedDomainTypeMirror);
            binder.readBean(typeNotes);
            typeNotesTextArea.setEnabled(true);
        });
        binder.forField(domainTypeComboBox)
            .asRequired("Type is required.")
            .bind(TypeNotes::getSelectedTypeMirror, TypeNotes::setSelectedTypeMirror);

        formLayout.addFormItem(domainTypeComboBox, "Type");
        formLayout.addFormItem(typeNotesTextArea, "Notes");

        return formLayout;
    }

    private Set<DomainTypeMirror> getAllDomainTypeMirrorsInIncludedPackages(List<DomainTypeMirror> domainTypeMirrors, Set<String> explicitlyIncludedPackagesNames) {
        return domainTypeMirrors.stream().filter(domainTypeMirror -> {
            if(explicitlyIncludedPackagesNames == null || explicitlyIncludedPackagesNames.isEmpty()) return true;
            return explicitlyIncludedPackagesNames.stream().anyMatch(packageName -> domainTypeMirror.getTypeName().startsWith(packageName));
        }).collect(Collectors.toSet());
    }

    private HorizontalLayout getButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);

        saveButton = new Button(new Icon(VaadinIcon.CHECK));
        saveButton.addClickListener(e -> {
            binder.writeBeanIfValid(typeNotes);
            diagramTypeNoteService.save(
                typeNotes.getNotes(),
                typeNotes.getSelectedTypeMirror(),
                diagram);
            ComponentUtil.fireEvent(UI.getCurrent(), new DiagramTypeNotesChangedEvent(this, false, null));
        });
        saveButton.getStyle().setMargin("0");

        Button cancelButton = new Button(new Icon(VaadinIcon.CLOSE));
        cancelButton.addClickListener(e -> ComponentUtil.fireEvent(UI.getCurrent(), new DiagramTypeNotesChangedEvent(this, false, null)));
        cancelButton.getStyle().setMargin("0");

        buttonLayout.add(saveButton, cancelButton);
        return buttonLayout;
    }

    public void setSelectedTypeMirrorName(String typeMirrorName) {
        DomainTypeMirror foundDomainTypeMirrorByName = domainTypeMirrors.stream().filter(
            domainTypeMirror -> Objects.equals(domainTypeMirror.getTypeName(), typeMirrorName)).findFirst().orElse(null);
        this.typeNotes = new TypeNotes(diagramTypeNoteService.getNotes(diagram, foundDomainTypeMirrorByName), foundDomainTypeMirrorByName);
        binder.readBean(typeNotes);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class TypeNotes {
        private String notes;
        private DomainTypeMirror selectedTypeMirror;
    }
}
