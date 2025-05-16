package io.domainlifecycles.diagramviewer.webapp.components.various.cards;

import com.vaadin.flow.component.orderedlayout.FlexLayout;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.DiagramDirectoryService;
import java.util.Set;

public class DiagramCardGridContainer extends FlexLayout {

    public DiagramCardGridContainer() {
        setSizeUndefined();
        setWidthFull();
    }

    public DiagramCardGridContainer(final DiagramDirectoryService diagramDirectoryService,
                                    final Project project,
                                    final Set<DiagramDirectory> diagramDirectories,
                                    final Set<Diagram> diagrams) {
        this();
        add(new DiagramCardGrid(diagramDirectoryService, project, diagramDirectories, diagrams));
    }

    public DiagramCardGridContainer(final DiagramDirectoryService diagramDirectoryService,
                                    final Project project,
                                    final Set<Diagram> diagrams) {
        this();
        add(new DiagramCardGrid(diagramDirectoryService, project, diagrams));
    }
}
