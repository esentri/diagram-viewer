package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.service.DiagramTypeNoteService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramTypeNotesChangedEvent;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DiagramViewNotesContainer extends VerticalLayout {

    private final Diagram diagram;
    private final DiagramTypeNoteService diagramTypeNoteService;

    public DiagramViewNotesContainer(Diagram diagram, DiagramTypeNoteService diagramTypeNoteService) {
        this.diagram = diagram;
        this.diagramTypeNoteService = diagramTypeNoteService;

        setPadding(false);
        setMargin(false);

        refreshNotes();
    }

    public void refreshNotes() {
        removeAll();
        Map<String, String> allDiagramTypeNotesByDomainTypeMirrorName = diagramTypeNoteService.getNotes(diagram);
        Map<String, String> allDiagramTypeNotesByDomainTypeMirrorNameFilteredByIncludedPackages =
            allDiagramTypeNotesByDomainTypeMirrorName.entrySet().stream()
                .filter(typeMirrorNoteEntry -> {
                    if (diagram.getDomainModelVisibility().getExplicitlyIncludedPackagesNames() == null ||
                        diagram.getDomainModelVisibility().getExplicitlyIncludedPackagesNames().isEmpty()) return true;

                    return diagram.getDomainModelVisibility().getExplicitlyIncludedPackagesNames().stream().anyMatch(
                        includedPackageName -> typeMirrorNoteEntry.getKey().startsWith(includedPackageName));
                })
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue
                ));

        List<Component> allTypeNotesInPackage =
            allDiagramTypeNotesByDomainTypeMirrorNameFilteredByIncludedPackages.keySet().stream().map(
            domainTypeMirrorName -> {
                TextArea textArea = new TextArea();
                textArea.setWidthFull();
                textArea.setLabel(domainTypeMirrorName);
                textArea.setReadOnly(true);
                textArea.setValue(allDiagramTypeNotesByDomainTypeMirrorName.get(domainTypeMirrorName));

                textArea.addFocusListener(e -> ComponentUtil.fireEvent(UI.getCurrent(),
                    new DiagramTypeNotesChangedEvent(this, false, domainTypeMirrorName)));

                return (Component) textArea;
            }).toList();

        add(allTypeNotesInPackage);
    }
}
