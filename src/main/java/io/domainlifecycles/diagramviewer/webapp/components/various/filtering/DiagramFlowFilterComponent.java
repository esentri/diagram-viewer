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

package io.domainlifecycles.diagramviewer.webapp.components.various.filtering;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.util.DomainModelUtils;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramReRenderedEvent;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.mirror.api.DomainCommandMirror;
import io.domainlifecycles.mirror.api.DomainEventMirror;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.mirror.api.MethodMirror;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Lets the user restrict a diagram to one or more flows: starting at a chosen class - and,
 * optionally, one of its methods - the diagram is trimmed to only the types reached by the analyzed
 * method calls of the domain.
 * <p>
 * This requires the result of a static analysis of the domain classes ({@code DomainCalls}) to have
 * been uploaded alongside the domain mirror; the filter is unavailable otherwise.
 */
@Slf4j
public class DiagramFlowFilterComponent extends Div {

    private static final String FLOW_METHOD_SEPARATOR = "#";

    private final SessionStorage sessionStorage;
    private final DiagramService diagramService;
    private Diagram currentDiagram;

    private ComboBox<DomainTypeMirror> classComboBox;
    private ComboBox<MethodMirror> methodComboBox;

    public DiagramFlowFilterComponent(
            SessionStorage sessionStorage,
            DiagramService diagramService) {
        this.sessionStorage = sessionStorage;
        this.diagramService = diagramService;
        setWidthFull();
    }

    public void setDiagram(Diagram diagram) {
        this.currentDiagram = diagram;
        refreshDetails();
    }

    private void refreshDetails() {
        removeAll();
        if (currentDiagram == null) {
            return;
        }
        log.debug("refreshDetails DiagramFlowFilterComponent started");

        Details flowDetails = new Details("Flow filter");
        flowDetails.setWidthFull();
        flowDetails.setOpened(sessionStorage.isFlowFilterOpen());
        flowDetails.addOpenedChangeListener(e -> sessionStorage.setFlowFilterOpen(e.isOpened()));

        boolean domainCallsAvailable = sessionStorage.getDomainCalls(currentDiagram.getProject().getId()).isPresent();
        if (!domainCallsAvailable) {
            flowDetails.add(new Paragraph(
                "Upload the result of a static code analysis (DomainCalls) alongside the domain mirror "
                    + "to restrict this diagram to one or more flows."));
            add(flowDetails);
            return;
        }

        List<DomainTypeMirror> domainTypeMirrors = sessionStorage.getAllDomainTypeMirrorsWithoutEnumsAndIds(
            currentDiagram.getProject().getId());
        List<DomainTypeMirror> selectableTypes = DomainModelUtils.filterConcreteMirrorsInterfaceAvailable(
            currentDiagram, domainTypeMirrors);

        classComboBox = new ComboBox<>("Class");
        classComboBox.setWidthFull();
        classComboBox.setItems(selectableTypes);
        classComboBox.setItemLabelGenerator(DomainModelUtils::nameWithStereoType);

        methodComboBox = new ComboBox<>("Method (optional, restricts to that method's flow)");
        methodComboBox.setWidthFull();
        methodComboBox.setItemLabelGenerator(this::methodLabel);
        methodComboBox.setEnabled(false);

        classComboBox.addValueChangeListener(e -> updateMethodComboBoxForSelectedClass(e.getValue()));

        Button addButton = new Button("Add flow", e -> addFlowStartingPoint());
        addButton.setWidthFull();

        HorizontalLayout selectionLayout = new HorizontalLayout(classComboBox, methodComboBox);
        selectionLayout.setWidthFull();
        selectionLayout.setFlexGrow(1, classComboBox, methodComboBox);

        flowDetails.add(selectionLayout, addButton);

        Set<String> activeFlows = currentDiagram.getDomainModelVisibility().getIncludeFlowsFrom();
        if (!activeFlows.isEmpty()) {
            MultiSelectComboBox<String> activeFlowsComboBox = new MultiSelectComboBox<>("Active flow filters:");
            activeFlowsComboBox.setWidthFull();
            activeFlowsComboBox.setItems(activeFlows);
            activeFlowsComboBox.setItemLabelGenerator(this::flowStartingPointLabel);
            activeFlowsComboBox.select(activeFlows);
            activeFlowsComboBox.addValueChangeListener(e -> applyFlowsFrom(new LinkedHashSet<>(e.getValue())));
            flowDetails.add(activeFlowsComboBox);
        }

        add(flowDetails);
        log.debug("refreshDetails DiagramFlowFilterComponent finished");
    }

    private void updateMethodComboBoxForSelectedClass(DomainTypeMirror selectedType) {
        methodComboBox.clear();
        if (selectedType == null || selectedType instanceof DomainCommandMirror || selectedType instanceof DomainEventMirror) {
            methodComboBox.setItems(List.of());
            methodComboBox.setEnabled(false);
            methodComboBox.setHelperText(selectedType == null
                ? null
                : "Starts the flow triggered by this " + (selectedType instanceof DomainCommandMirror ? "command" : "event") + ".");
            return;
        }

        List<MethodMirror> methods = selectedType.getMethods().stream()
            .sorted(Comparator.comparing(MethodMirror::getName))
            .toList();
        methodComboBox.setHelperText("Leave empty to include the flows of all methods.");
        methodComboBox.setItems(methods);
        methodComboBox.setEnabled(true);
    }

    private void addFlowStartingPoint() {
        DomainTypeMirror selectedType = classComboBox.getValue();
        if (selectedType == null) {
            return;
        }
        MethodMirror selectedMethod = methodComboBox.isEnabled() ? methodComboBox.getValue() : null;
        String flowStartingPoint = selectedMethod == null
            ? selectedType.getTypeName()
            : selectedType.getTypeName() + FLOW_METHOD_SEPARATOR + selectedMethod.getName();

        Set<String> updatedFlows = new LinkedHashSet<>(currentDiagram.getDomainModelVisibility().getIncludeFlowsFrom());
        updatedFlows.add(flowStartingPoint);
        applyFlowsFrom(updatedFlows);
    }

    private void applyFlowsFrom(Set<String> newFlows) {
        DomainModelVisibility newVisibility = currentDiagram.getDomainModelVisibility().replaceIncludeFlowsFrom(newFlows);
        currentDiagram.setDomainModelVisibility(newVisibility);
        currentDiagram = diagramService.updateModelAndImage(currentDiagram);
        ComponentUtil.fireEvent(UI.getCurrent(), new DiagramReRenderedEvent(this, false));
        refreshDetails();
    }

    private String methodLabel(MethodMirror method) {
        String params = method.getParameters().stream()
            .map(param -> shortTypeName(param.getType().getTypeName()))
            .collect(Collectors.joining(", "));
        return method.getName() + "(" + params + ")";
    }

    private String flowStartingPointLabel(String flowStartingPoint) {
        int separatorIndex = flowStartingPoint.indexOf(FLOW_METHOD_SEPARATOR);
        if (separatorIndex < 0) {
            return shortTypeName(flowStartingPoint);
        }
        return shortTypeName(flowStartingPoint.substring(0, separatorIndex))
            + FLOW_METHOD_SEPARATOR + flowStartingPoint.substring(separatorIndex + FLOW_METHOD_SEPARATOR.length());
    }

    private String shortTypeName(String fullyQualifiedTypeName) {
        return fullyQualifiedTypeName.substring(fullyQualifiedTypeName.lastIndexOf('.') + 1);
    }
}
