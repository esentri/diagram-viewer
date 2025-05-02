package io.domainlifecycles.diagramviewer.webapp.components.various.cards;

import com.vaadin.flow.component.orderedlayout.FlexLayout;
import io.domainlifecycles.diagramviewer.model.Project;

public class DiagramCardGridContainer extends FlexLayout {

    public DiagramCardGridContainer(final Project project) {
        setSizeUndefined();
        setWidthFull();

        add(new DiagramCardGrid(project));
    }
}
