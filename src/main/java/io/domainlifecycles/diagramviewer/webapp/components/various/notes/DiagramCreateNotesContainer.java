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
 *  Copyright 2019-2025 the original author or authors.
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

package io.domainlifecycles.diagramviewer.webapp.components.various.notes;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DetachEvent;
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
import com.vaadin.flow.shared.Registration;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramTypeNote;
import io.domainlifecycles.diagramviewer.service.DiagramTypeNoteService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramTypeNotesChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DiagramCreateNotesContainer extends VerticalLayout {

    private final static double STRING_LENGTH_TO_REM_FACTOR = 0.6;


    private final DiagramTypeNoteService diagramTypeNoteService;
    private final SessionStorage sessionStorage;
    private final Binder<TypeNotes> binder;
    private ComboBox<DomainTypeMirror> domainTypeComboBox;

    private TypeNotes typeNotes;
    private Button saveButton;
    private Registration registrationDomainType;

    private Diagram diagram;

    public DiagramCreateNotesContainer(
            DiagramTypeNoteService diagramTypeNoteService,
            SessionStorage sessionStorage
    ) {
        this.diagramTypeNoteService = diagramTypeNoteService;
        this.sessionStorage = sessionStorage;
        this.binder = new Binder<>();

        setPadding(false);
        setMargin(false);

        add(getButtonLayout(), createFormLayout());
        binder.addStatusChangeListener(event -> saveButton.setEnabled(binder.isValid()));
    }

    public void switchDiagram(Diagram diagram) {
        this.diagram = diagram;
        Set<DomainTypeMirror> allDomainTypeMirrorsInIncludedPackages = getAllDomainTypeMirrorsInIncludedPackages(
                sessionStorage.getAllDomainTypeMirrorsWithoutEnumsAndIds(diagram.getProject().getId()),
                diagram.getDomainModelVisibility().getExplicitlyIncludedPackagesNames()
        );
        int longestDomainTypeMirrorNameLength = allDomainTypeMirrorsInIncludedPackages.stream()
                .map(DomainTypeMirror::getTypeName)
                .max(Comparator.comparingInt(String::length))
                .orElse("").length();
        domainTypeComboBox.setItems(allDomainTypeMirrorsInIncludedPackages);
        getStyle().set("--vaadin-combo-box-overlay-width", longestDomainTypeMirrorNameLength * STRING_LENGTH_TO_REM_FACTOR + "rem");
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

        domainTypeComboBox = new ComboBox<>();

        domainTypeComboBox.setItemLabelGenerator(DomainTypeMirror::getTypeName);
        domainTypeComboBox.setWidthFull();

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

    private void setSelectedTypeMirrorName(String typeMirrorName) {

        DomainTypeMirror foundDomainTypeMirrorByName = sessionStorage.getAllDomainTypeMirrorsWithoutEnumsAndIds(
                diagram.getProject().getId()
            )
            .stream()
            .filter(domainTypeMirror -> Objects.equals(domainTypeMirror.getTypeName(), typeMirrorName))
            .findFirst().orElse(null);
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

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        registrationDomainType =
                ComponentUtil.addListener(
                        UI.getCurrent(),
                        DiagramTypeNotesChangedEvent.class,
                        event ->  setSelectedTypeMirrorName(event.getTypeMirrorName())
                );
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        registrationDomainType.remove();
    }

}
