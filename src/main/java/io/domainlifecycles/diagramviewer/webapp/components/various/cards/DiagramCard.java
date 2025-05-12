package io.domainlifecycles.diagramviewer.webapp.components.various.cards;

import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.CreateFolderDialog;

public class DiagramCard extends Card implements DragSource<DiagramCardLinkWrapper>, DropTarget<DiagramCardLinkWrapper> {

    public DiagramCard(final Diagram diagram, final String diagramSrc) {
        setTitle(diagram.getFileName());

        Image image = new Image(diagramSrc, diagram.getFileName());
        image.setHeight("200px");
        image.setWidth("95%");
        setMedia(image);

        configureDragAndDrop(diagram);
    }

    private void configureDragAndDrop(Diagram diagram) {
        DragSource.create(this);
        DropTarget.create(this);

        setDragData(diagram);
        setDropEffect(DropEffect.COPY);

        addDropListener(event -> {
            Diagram draggedDiagram = (Diagram) event.getDragData().orElseThrow();
            CreateFolderDialog createFolderDialog = new CreateFolderDialog(draggedDiagram, diagram);
            createFolderDialog.open();
        });

        addDragStartListener(event -> setActive(false));
        addDragEndListener(event -> setActive(true));
    }
}
