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
