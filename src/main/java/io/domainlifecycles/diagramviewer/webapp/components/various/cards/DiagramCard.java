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

package io.domainlifecycles.diagramviewer.webapp.components.various.cards;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.Image;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.service.DiagramDirectoryService;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.CreateFolderDialog;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;

public class DiagramCard extends Card implements DragSource<CardLinkWrapper>, DropTarget<CardLinkWrapper> {

    public DiagramCard(final DiagramDirectoryService diagramDirectoryService, final Diagram diagram, final String diagramSrc) {
        setTitle(diagram.getName());

        Image image = new Image(diagramSrc, diagram.getName());
        image.setHeight("200px");
        image.setWidth("95%");
        setMedia(image);

        configureDragAndDrop(diagram, diagramDirectoryService);
    }

    public DiagramCard(final DiagramDirectoryService diagramDirectoryService, final DiagramDirectory diagramDirectory) {
        setTitle(diagramDirectory.getName());

        Image image = new Image("frontend/icons/folder-open-o.svg", "Directory");
        image.setHeight("200px");
        image.setWidth("95%");
        setMedia(image);

        configureDragAndDrop(diagramDirectory, diagramDirectoryService);
    }

    private void configureDragAndDrop(Diagram diagram, DiagramDirectoryService diagramDirectoryService) {
        DragSource.create(this);
        DropTarget.create(this);

        setDragData(diagram);
        setDropEffect(DropEffect.COPY);

        addDropListener(event -> {
            Diagram draggedDiagram = (Diagram) event.getDragData().orElseThrow();
            CreateFolderDialog createFolderDialog = new CreateFolderDialog(diagram.getProject(), draggedDiagram, diagram, diagramDirectoryService);
            createFolderDialog.open();
        });

        addDragStartListener(event -> setActive(false));
        addDragEndListener(event -> setActive(true));
    }

    private void configureDragAndDrop(DiagramDirectory diagramDirectory, DiagramDirectoryService diagramDirectoryService) {
        DropTarget.create(this);

        setDraggable(false);
        setDropEffect(DropEffect.COPY);

        addDropListener(event -> {
            Diagram draggedDiagram = (Diagram) event.getDragData().orElseThrow();
            diagramDirectoryService.add(diagramDirectory, draggedDiagram);
            ComponentUtil.fireEvent(UI.getCurrent(), new DiagramsOrProjectsChangedEvent(this, false));
        });
    }
}
