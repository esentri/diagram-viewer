package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramTypeNote;
import io.domainlifecycles.diagramviewer.service.DiagramTypeNoteService;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class DiagramTypeNotesDialog extends Dialog {

    private final Diagram diagram;
    private final List<DomainTypeMirror> domainTypeMirrors;
    private final DiagramTypeNoteService diagramTypeNoteService;
    private final Binder<TypeNotes> binder;

    private TypeNotes typeNotes;
    private Button saveButton;

    public DiagramTypeNotesDialog(Diagram diagram, List<DomainTypeMirror> allDomainTypeMirrors, DiagramTypeNoteService diagramTypeNoteService) {
        this.diagram = diagram;
        this.domainTypeMirrors = allDomainTypeMirrors;
        this.diagramTypeNoteService = diagramTypeNoteService;
        this.binder = new Binder<>();

        setHeaderTitle("Type notes");
        setWidth("50%");
        setHeight("70%");

        getFooter().add(createSaveButton());
        getFooter().add(createCancelButton());
        add(createDialogLayout());

        addOpenedChangeListener(e -> {
            if(e.isOpened()) {
                this.typeNotes = new TypeNotes();
                binder.readBean(typeNotes);
            }
        });

        binder.addStatusChangeListener(event -> saveButton.setEnabled(binder.isValid()));
    }

    private Button createSaveButton() {
        saveButton = new Button("Save");

        saveButton.addClickListener(e -> {
            binder.writeBeanIfValid(typeNotes);
            diagramTypeNoteService.save(
                typeNotes.getNotes(),
                typeNotes.getSelectedTypeMirror(),
                diagram);
            close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return saveButton;
    }

    private Button createCancelButton() {
        return new Button("Cancel", e -> close());
    }

    private FormLayout createDialogLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        formLayout.setSizeFull();

        TextArea typeNotesTextArea = new TextArea();
        typeNotesTextArea.setWidthFull();
        typeNotesTextArea.setHeight("400px");
        typeNotesTextArea.setMaxLength(DiagramTypeNote.NOTES_MAX_LENGTH);
        typeNotesTextArea.setClearButtonVisible(true);
        typeNotesTextArea.setEnabled(false);
        typeNotesTextArea.addValueChangeListener(e -> e.getSource()
            .setHelperText(e.getValue().length() + "/" + DiagramTypeNote.NOTES_MAX_LENGTH));
        typeNotesTextArea.setValueChangeMode(ValueChangeMode.EAGER);
        binder.forField(typeNotesTextArea)
            .bind(TypeNotes::getNotes, TypeNotes::setNotes);

        Select<DomainTypeMirror> domainTypeSelect = new Select<>();
        domainTypeSelect.setItems(domainTypeMirrors);
        domainTypeSelect.setItemLabelGenerator(DomainTypeMirror::getTypeName);
        domainTypeSelect.setWidthFull();
        domainTypeSelect.addValueChangeListener(e -> {
            DomainTypeMirror selectedDomainTypeMirror = e.getValue();
            String notes = diagramTypeNoteService.getNotes(diagram, selectedDomainTypeMirror);
            typeNotes = new TypeNotes(notes, selectedDomainTypeMirror);
            binder.readBean(typeNotes);
            typeNotesTextArea.setEnabled(true);
        });
        binder.forField(domainTypeSelect)
            .asRequired("Type is required.")
            .bind(TypeNotes::getSelectedTypeMirror, TypeNotes::setSelectedTypeMirror);

        formLayout.addFormItem(domainTypeSelect, "Type");
        formLayout.addFormItem(typeNotesTextArea, "Notes");

        return formLayout;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class TypeNotes {
        private String notes;
        private DomainTypeMirror selectedTypeMirror;
    }
}
