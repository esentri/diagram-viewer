package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramStylingChangedEvent;

public class DiagramPackageFilterAccordionComponent extends Accordion {

    private final Diagram diagram;
    private final Project project;
    private final DiagramService diagramService;

    public DiagramPackageFilterAccordionComponent(Diagram diagram, Project project, DiagramService diagramService) {
        this.diagram = diagram;
        this.project = project;
        this.diagramService = diagramService;
        createAccordion();
    }

    private void createAccordion() {
        AccordionPanel packageFilterPanel = new AccordionPanel();
        packageFilterPanel.setSummaryText("Package Filter");

        PackageSelectChipField packageSelectChipField =
            new PackageSelectChipField(diagram.getDomainModelVisibility().getFilteredPackageNames());
        packageSelectChipField.setWidthFull();
        packageSelectChipField.addValueChangeListener(e -> {
            diagram.setDomainModelVisibility(diagram.getDomainModelVisibility().replaceFilteredPackageNames(e.getValue()));
            diagramService.update(diagram, project);
            ComponentUtil.fireEvent(
                UI.getCurrent(), new DiagramStylingChangedEvent(this, false));
        });

        packageFilterPanel.add(packageSelectChipField);
        add(packageFilterPanel);
    }
}
