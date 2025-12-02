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
        diagramNotesComponentsContainer = new DiagramNotesComponentsContainer(
                diagramService,
                diagramTypeNoteService,
                sessionStorage
        );
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
