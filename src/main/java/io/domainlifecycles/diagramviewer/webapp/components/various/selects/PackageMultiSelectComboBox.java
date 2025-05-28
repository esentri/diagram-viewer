package io.domainlifecycles.diagramviewer.webapp.components.various.selects;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.webapp.components.various.filtering.DiagramFilterComponent;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PackageMultiSelectComboBox extends MultiSelectComboBox<String> {

    private final static double STRING_LENGTH_TO_REM_FACTOR = 0.6;

    public PackageMultiSelectComboBox(List<DomainTypeMirror> domainTypeMirrors, Diagram diagram) {
        this(domainTypeMirrors);
        setValue(diagram.getDomainModelVisibility().getExplicitlyIncludedPackagesNames());
    }

    public PackageMultiSelectComboBox(List<DomainTypeMirror> domainTypeMirrors) {

        List<String> packages = buildPackageNamesSorted(domainTypeMirrors);
        int longestItemLength = packages.stream()
            .max(Comparator.comparingInt(String::length))
            .orElse("").length();

        setItems(packages);

        setWidthFull();
        getStyle().set("--vaadin-multi-select-combo-box-overlay-width", longestItemLength * STRING_LENGTH_TO_REM_FACTOR + "rem");
    }

    private List<String> buildPackageNamesSorted(List<DomainTypeMirror> domainTypeMirrors) {
        Set<String> result = new HashSet<>();

        for (DomainTypeMirror type : domainTypeMirrors) {
            String typeName = type.getTypeName();
            int indexOfLastDot = typeName.lastIndexOf('.');
            if (indexOfLastDot == -1 || typeName.startsWith(DiagramFilterComponent.DOMAINLIFECYCLES_PACKAGE_NAME)) continue;

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
