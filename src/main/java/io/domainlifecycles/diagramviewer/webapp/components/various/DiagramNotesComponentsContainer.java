package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.service.DiagramTypeNoteService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramTypeNotesChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.events.ProjectUsersChangedEvent;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.util.List;

public class DiagramNotesComponentsContainer extends VerticalLayout {

    private final Diagram diagram;
    private final List<DomainTypeMirror> domainTypeMirrors;
    private final DiagramTypeNoteService diagramTypeNoteService;
    private final DiagramViewNotesContainer notesViewContainer;
    private final DiagramCreateNotesContainer diagramCreateNotesContainer;
    private final Button addNotesButton;

    private boolean isInViewMode;
    private Button saveButton;
    private Registration registration;

    public DiagramNotesComponentsContainer(Diagram diagram, List<DomainTypeMirror> allDomainTypeMirrors, DiagramTypeNoteService diagramTypeNoteService) {
        setPadding(false);
        setMargin(false);
        getStyle().set("overflow-x", "hidden");

        this.isInViewMode = true;
        this.diagram = diagram;
        this.domainTypeMirrors = allDomainTypeMirrors;
        this.diagramTypeNoteService = diagramTypeNoteService;

        addNotesButton = getAddNotesButton();
        add(addNotesButton);

        notesViewContainer = new DiagramViewNotesContainer(diagram, diagramTypeNoteService);
        diagramCreateNotesContainer = new DiagramCreateNotesContainer(domainTypeMirrors, diagramTypeNoteService, diagram);

        add(notesViewContainer);
    }

    private Button getAddNotesButton() {
        Button addNotesButton = new Button("Add", new Icon(VaadinIcon.PLUS));
        addNotesButton.addClickListener(e -> switchNotesView(null));
        return addNotesButton;
    }

    private void switchNotesView(String typeMirrorName) {
        this.removeAll();
        if(isInViewMode) {
            add(diagramCreateNotesContainer);
            diagramCreateNotesContainer.setSelectedTypeMirrorName(typeMirrorName);
        }
        else {
            add(addNotesButton);
            add(notesViewContainer);
            notesViewContainer.refreshNotes();
        }
        isInViewMode = !isInViewMode;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        registration =
            ComponentUtil.addListener(
                attachEvent.getUI(),
                DiagramTypeNotesChangedEvent.class,
                event ->  switchNotesView(event.getTypeMirrorName())
            );
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        registration.remove();
    }
}
