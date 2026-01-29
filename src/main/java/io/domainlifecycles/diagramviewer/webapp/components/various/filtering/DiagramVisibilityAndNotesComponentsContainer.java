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

package io.domainlifecycles.diagramviewer.webapp.components.various.filtering;

import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.Scroller.ScrollDirection;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.DiagramTypeNoteService;
import io.domainlifecycles.diagramviewer.webapp.components.various.notes.DiagramNotesComponentsContainer;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import lombok.extern.slf4j.Slf4j;
import org.vaadin.addons.taefi.component.ToggleButtonGroup;

import java.util.List;

@Slf4j
public class DiagramVisibilityAndNotesComponentsContainer extends VerticalLayout {

    private final DiagramVisibilityComponentsContainer diagramVisibilityComponentsContainer;
    private final DiagramNotesComponentsContainer diagramNotesComponentsContainer;
    private final Scroller scroller;
    private Diagram currentDiagram;


    public DiagramVisibilityAndNotesComponentsContainer(
            SessionStorage sessionStorage,
            DiagramService diagramService,
            DiagramTypeNoteService diagramTypeNoteService
    ) {
        log.debug("creating DiagramVisibilityAndNotesComponentsContainer started");
        setPadding(false);
        setMargin(false);
        setHeightFull();
        setWidth("30%");
        ToggleButtonGroup<SelectableView> toggleButtonGroup = new ToggleButtonGroup<>(List.of(SelectableView.VISIBILITY, SelectableView.NOTES));
        toggleButtonGroup.setItemLabelGenerator(SelectableView::getLabel);
        toggleButtonGroup.addValueChangeListener(e -> switchDisplayedContent(e.getValue()));
        log.debug("creating DiagramVisibilityComponentsContainer started");
        diagramVisibilityComponentsContainer = new DiagramVisibilityComponentsContainer(sessionStorage, diagramService);
        log.debug("creating DiagramVisibilityComponentsContainer finished");
        log.debug("creating DiagramNotesComponentsContainer started");
        diagramNotesComponentsContainer = new DiagramNotesComponentsContainer(diagramTypeNoteService, sessionStorage);
        log.debug("creating DiagramNotesComponentsContainer finished");
        scroller = new Scroller();
        scroller.setScrollDirection(ScrollDirection.BOTH);
        scroller.setWidthFull();
        toggleButtonGroup.setValue(SelectableView.VISIBILITY);
        add(toggleButtonGroup, scroller);
        log.debug("creating DiagramVisibilityAndNotesComponentsContainer finished");
    }


    private void switchDisplayedContent(SelectableView selectedView) {
        if(selectedView!=null) {
            switch (selectedView) {
                case NOTES -> {
                    scroller.setContent(diagramNotesComponentsContainer);
                    diagramNotesComponentsContainer.setDiagram(currentDiagram);
                }
                case VISIBILITY -> {
                    scroller.setContent(diagramVisibilityComponentsContainer);
                    diagramVisibilityComponentsContainer.setDiagram(currentDiagram);
                }
            }
        }else{
            scroller.setContent(null);
        }
    }

    public void setDiagram(Diagram diagram) {
        this.currentDiagram = diagram;
        diagramNotesComponentsContainer.setDiagram(currentDiagram);
        diagramVisibilityComponentsContainer.setDiagram(currentDiagram);
    }

    private enum SelectableView {
        VISIBILITY("Visibility"),
        NOTES("Notes");
        final String label;
        public String getLabel() {
            return label;
        }
        SelectableView(String label) {
            this.label = label;
        }
    }

}
