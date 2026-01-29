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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.service.DiagramTypeNoteService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramTypeNotesChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;

public class DiagramNotesComponentsContainer extends VerticalLayout {

    private final DiagramViewNotesContainer notesViewContainer;
    private final DiagramCreateNotesContainer diagramCreateNotesContainer;
    private final Button addNotesButton;
    private Diagram currentDiagram;
    private Registration registrationDomainType;

    private boolean isInViewMode = false;

    public DiagramNotesComponentsContainer(
            DiagramTypeNoteService diagramTypeNoteService,
            SessionStorage sessionStorage
    ) {
        setPadding(false);
        setMargin(false);
        getStyle().set("overflow-x", "hidden");

        addNotesButton = getAddNotesButton();
        add(addNotesButton);
        diagramCreateNotesContainer = new DiagramCreateNotesContainer(diagramTypeNoteService, sessionStorage);
        add(diagramCreateNotesContainer);
        notesViewContainer = new DiagramViewNotesContainer(diagramTypeNoteService);
        add(notesViewContainer);
        switchNotesView();
    }

    private Button getAddNotesButton() {
        Button addNotesButton = new Button("Add", new Icon(VaadinIcon.PLUS));
        addNotesButton.addClickListener(e -> switchNotesView());
        return addNotesButton;
    }

    private void switchNotesView() {
        isInViewMode = !isInViewMode;
        this.diagramCreateNotesContainer.setVisible(!isInViewMode);
        this.addNotesButton.setVisible(isInViewMode);
        this.notesViewContainer.setVisible(isInViewMode);
        if(currentDiagram!=null) {
            if (isInViewMode) {
                this.notesViewContainer.refreshNotes(currentDiagram);
            } else {
                this.diagramCreateNotesContainer.switchDiagram(currentDiagram);
            }
        }
    }

    public void setDiagram(Diagram diagram) {
        currentDiagram = diagram;
        diagramCreateNotesContainer.switchDiagram(diagram);
        notesViewContainer.refreshNotes(diagram);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        registrationDomainType =
                ComponentUtil.addListener(
                    UI.getCurrent(),
                    DiagramTypeNotesChangedEvent.class,
                    event ->  {
                        if(event.getTypeMirrorName() == null){
                            switchNotesView();
                        }
                    }
                );
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        registrationDomainType.remove();
    }


}
