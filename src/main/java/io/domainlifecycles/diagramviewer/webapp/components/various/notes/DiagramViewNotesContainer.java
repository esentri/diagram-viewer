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

package io.domainlifecycles.diagramviewer.webapp.components.various.notes;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.service.DiagramTypeNoteService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramTypeNotesChangedEvent;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DiagramViewNotesContainer extends VerticalLayout {


    private final DiagramTypeNoteService diagramTypeNoteService;

    public DiagramViewNotesContainer(
            DiagramTypeNoteService diagramTypeNoteService
    ) {

        this.diagramTypeNoteService = diagramTypeNoteService;
        setPadding(false);
        setMargin(false);
    }

    public void refreshNotes(Diagram diagram) {
        removeAll();
        Map<String, String> allDiagramTypeNotesByDomainTypeMirrorName = diagramTypeNoteService.getNotes(diagram);
        Map<String, String> allDiagramTypeNotesByDomainTypeMirrorNameFilteredByIncludedPackages =
            allDiagramTypeNotesByDomainTypeMirrorName.entrySet().stream()
                .filter(typeMirrorNoteEntry -> {
                    if (diagram.getDomainModelVisibility().getExplicitlyIncludedPackagesNames() == null ||
                        diagram.getDomainModelVisibility().getExplicitlyIncludedPackagesNames().isEmpty()) return true;

                    return diagram.getDomainModelVisibility().getExplicitlyIncludedPackagesNames().stream().anyMatch(
                        includedPackageName -> typeMirrorNoteEntry.getKey().startsWith(includedPackageName));
                })
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue
                ));

        List<Component> allTypeNotesInPackage =
            allDiagramTypeNotesByDomainTypeMirrorNameFilteredByIncludedPackages.keySet().stream().map(
            domainTypeMirrorName -> {
                TextArea notesTextArea = new TextArea();
                notesTextArea.setWidthFull();
                notesTextArea.setLabel(domainTypeMirrorName.substring(domainTypeMirrorName.lastIndexOf('.') + 1));
                notesTextArea.setReadOnly(true);
                notesTextArea.setValue(allDiagramTypeNotesByDomainTypeMirrorName.get(domainTypeMirrorName));
                notesTextArea.addClassName("notes-text-area");

                notesTextArea.addFocusListener(e -> ComponentUtil.fireEvent(UI.getCurrent(),
                    new DiagramTypeNotesChangedEvent(this, false, domainTypeMirrorName)));

                return (Component) notesTextArea;
            }).toList();

        add(allTypeNotesInPackage);
    }
}
