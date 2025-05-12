package io.domainlifecycles.diagramviewer.webapp.components.various.cards;

import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.Image;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.DiagramDirectory;
import io.domainlifecycles.diagramviewer.service.DiagramDirectoryService;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.CreateFolderDialog;

public class DiagramCard extends Card implements DragSource<CardLinkWrapper>, DropTarget<CardLinkWrapper> {

    public DiagramCard(final DiagramDirectoryService diagramDirectoryService, final Diagram diagram, final String diagramSrc) {
        setTitle(diagram.getFileName());

        Image image = new Image(diagramSrc, diagram.getFileName());
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
        });
    }
}
