package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramStylingChangedEvent;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static io.domainlifecycles.diagramviewer.webapp.components.various.DiagramFilterComponent.DOMAINLIFECYCLES_PACKAGE_NAME;

public class PackageMultiSelectComboBox extends MultiSelectComboBox<String> {

    private final static double STRING_LENGTH_TO_REM_FACTOR = 0.6;

    public PackageMultiSelectComboBox(List<DomainTypeMirror> domainTypeMirrors, Diagram diagram, Project project, DiagramService diagramService) {

        List<String> packages = buildPackageNamesSorted(domainTypeMirrors);
        int longestItemLength = packages.stream()
            .max(Comparator.comparingInt(String::length))
            .orElse("").length();

        setItems(packages);

        setWidthFull();
        setValue(diagram.getDomainModelVisibility().getExplicitlyIncludedPackagesNames());
        getStyle().set("--vaadin-multi-select-combo-box-overlay-width", longestItemLength * STRING_LENGTH_TO_REM_FACTOR + "rem");
        addValueChangeListener(e -> {
            diagram.setDomainModelVisibility(diagram.getDomainModelVisibility().replaceExplicitlyIncludedPackagesNames(e.getValue()));
            diagramService.update(diagram, project);
            ComponentUtil.fireEvent(
                UI.getCurrent(), new DiagramStylingChangedEvent(this, false));
        });
    }

    private List<String> buildPackageNamesSorted(List<DomainTypeMirror> domainTypeMirrors) {
        Set<String> result = new HashSet<>();

        for (DomainTypeMirror type : domainTypeMirrors) {
            String typeName = type.getTypeName();
            int indexOfLastDot = typeName.lastIndexOf('.');
            if (indexOfLastDot == -1 || typeName.startsWith(DOMAINLIFECYCLES_PACKAGE_NAME)) continue;

            String fullPackage = typeName.substring(0, indexOfLastDot);
            String[] parts = fullPackage.split("\\.");

            if (parts.length < 3) continue;

            StringBuilder current = new StringBuilder(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                current.append('.').append(parts[i]);
                if (i >= 2) {
                    result.add(current.toString());
                }
            }
        }

        List<String> sorted = new ArrayList<>(result);
        sorted.sort(Comparator
            .comparingInt((String s) -> s.split("\\.").length)
            .thenComparing(Comparator.naturalOrder()));

        return sorted;
    }
}
