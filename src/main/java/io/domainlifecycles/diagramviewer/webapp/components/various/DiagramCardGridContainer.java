package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.orderedlayout.FlexLayout;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.webapp.components.various.cards.DiagramCardGrid;

public class DiagramCardGridContainer extends FlexLayout {

    private DiagramCardGrid diagramCardGrid;

    public DiagramCardGridContainer() {
        setMinHeight("100%");
        setMaxHeight("100%");
        setSizeFull();
    }

    public void reloadDiagramCardGrid(final Project project) {
        if(diagramCardGrid != null) {
            removeAll();
        }

        diagramCardGrid = new DiagramCardGrid(project);
        add(diagramCardGrid);
    }
}
