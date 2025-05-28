package io.domainlifecycles.diagramviewer.webapp.components.various.filtering;

import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.Scroller.ScrollDirection;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.DiagramTypeNoteService;
import io.domainlifecycles.diagramviewer.webapp.components.various.notes.DiagramNotesComponentsContainer;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.util.List;
import org.vaadin.addons.taefi.component.ToggleButtonGroup;

public class DiagramVisibilityAndNotesComponentsContainer extends VerticalLayout {

    private final DiagramVisibilityComponentsContainer diagramVisibilityComponentsContainer;
    private final DiagramNotesComponentsContainer diagramNotesComponentsContainer;
    private final Scroller scroller;

    public DiagramVisibilityAndNotesComponentsContainer(SessionStorage sessionStorage, Project project, Diagram diagram, List<DomainTypeMirror> domainTypeMirrors,
                                                        DiagramService diagramService, DiagramTypeNoteService diagramTypeNoteService) {

        setPadding(false);
        setMargin(false);
        setHeightFull();
        setWidth("30%");

        ToggleButtonGroup<SelectableView> toggleButtonGroup = new ToggleButtonGroup<>(List.of(SelectableView.VISIBILITY, SelectableView.NOTES));
        toggleButtonGroup.setItemLabelGenerator(SelectableView::getLabel);
        toggleButtonGroup.addValueChangeListener(e -> switchDisplayedContent(e.getValue()));

        diagramVisibilityComponentsContainer = new DiagramVisibilityComponentsContainer(sessionStorage, project,
            diagram, domainTypeMirrors, diagramService);
        diagramNotesComponentsContainer = new DiagramNotesComponentsContainer(diagram, domainTypeMirrors, diagramTypeNoteService);

        scroller = new Scroller();
        scroller.setScrollDirection(ScrollDirection.BOTH);
        scroller.setWidthFull();

        toggleButtonGroup.setValue(SelectableView.VISIBILITY);
        add(toggleButtonGroup, scroller);
    }

    private void switchDisplayedContent(SelectableView selectedView) {
        switch (selectedView) {
            case NOTES -> scroller.setContent(diagramNotesComponentsContainer);
            case VISIBILITY -> scroller.setContent(diagramVisibilityComponentsContainer);
        }
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
