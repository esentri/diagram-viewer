/*
 *     ________  .__
 *     \______ \ |__|____     ________________    _____
 *      |    |  \|  \__  \   / ___\_  __ \__  \  /     \
 *      |    |   \  |/ __ \_/ /_/  >  | \// __ \|  Y Y  \
 *     /_______  /__(____  /\___  /|__|  (____  /__|_|  /
 *             \/        \//_____/            \/      \/
 *     ____   ____.__
 *     \   \ /   /|__| ______  _  __ ___________
 *      \   Y   / |  |/ __ \ \/ \/ // __ \_  __ \
 *       \     /  |  \  ___/\     /\  ___/|  | \/
 *        \___/   |__|\___  >\/\_/  \___  >__|
 *                        \/            \/
 *
 *  Copyright 2025-2026 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.domainlifecycles.diagramviewer.webapp.components.various.selects;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.util.DomainModelUtils;
import io.domainlifecycles.diagramviewer.webapp.components.various.filtering.DiagramFilterComponent;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PackageMultiSelectComboBox extends MultiSelectComboBox<String> {

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
            if (indexOfLastDot == -1 || typeName.startsWith(DomainModelUtils.DOMAINLIFECYCLES_PACKAGE_NAME)) continue;

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
