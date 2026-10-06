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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.service.DiagramDirectoryService;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.CreateFolderDialog;
import io.domainlifecycles.diagramviewer.webapp.components.various.WrappableName;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import java.util.Locale;

public class DiagramCard extends Card implements DragSource<CardLinkWrapper>, DropTarget<CardLinkWrapper> {

    /** styled to wrap long names within the card, see diagram-viewer-styles.css */
    static final String CSS_CLASS = "diagram-card";

    /**
     * @param imageSize        the size of the diagram's image in bytes, {@code -1} if not rendered yet
     * @param previewLimitBytes up to which size the image itself is the preview; larger diagrams show a placeholder,
     *                         since drawing dozens of large images at once (e.g. a folder of flow diagrams created by
     *                         "Analyze Bounded Contexts") makes the browser slow
     */
    public DiagramCard(final DiagramDirectoryService diagramDirectoryService, final Diagram diagram, final long imageSize,
                       final long previewLimitBytes, final String diagramSrc) {
        addClassName(CSS_CLASS);
        setTitle(WrappableName.create(diagram.getName()));

        if (imageSize > previewLimitBytes) {
            setMedia(createPlaceholder(VaadinIcon.FILE_PICTURE,
                String.format(Locale.ROOT, "Large diagram (%.1f MB)", imageSize / 1024.0 / 1024.0), "Open it to view"));
        } else if (imageSize < 0) {
            setMedia(createPlaceholder(VaadinIcon.HOURGLASS, "Not rendered yet", ""));
        } else {
            Image image = new Image(diagramSrc, diagram.getName());
            image.setHeight("200px");
            image.setWidth("95%");
            // only the cards in view load their images
            image.getElement().setAttribute("loading", "lazy");
            setMedia(image);
        }

        configureDragAndDrop(diagram, diagramDirectoryService);
    }

    private static Div createPlaceholder(VaadinIcon icon, String text, String hint) {
        Icon placeholderIcon = icon.create();
        placeholderIcon.setSize("3rem");
        placeholderIcon.getStyle().set("color", "var(--lumo-contrast-40pct)");
        Span textSpan = new Span(text);
        Span hintSpan = new Span(hint);
        hintSpan.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", "var(--lumo-secondary-text-color)");
        Div placeholder = new Div(placeholderIcon, textSpan, hintSpan);
        placeholder.addClassName("diagram-card-placeholder");
        placeholder.setHeight("200px");
        placeholder.setWidth("95%");
        placeholder.getStyle()
            .set("display", "flex").set("flex-direction", "column").set("align-items", "center")
            .set("justify-content", "center").set("gap", "var(--lumo-space-xs)")
            .set("background", "var(--lumo-contrast-5pct)").set("border-radius", "var(--lumo-border-radius-m)");
        return placeholder;
    }

    public DiagramCard(final DiagramDirectoryService diagramDirectoryService, final DiagramDirectory diagramDirectory) {
        addClassName(CSS_CLASS);
        setTitle(WrappableName.create(diagramDirectory.getName()));

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
