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
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Lets the user restrict a diagram to one or more flows, in either direction:
 * <ul>
 *   <li>forward ("flows from"): starting at a chosen class - and, optionally, one of its methods -
 *   the diagram is trimmed to the types reached by the analyzed method calls of the domain, i.e.
 *   "what does this lead to"</li>
 *   <li>backward ("flows to"): the diagram is trimmed to the types leading into a chosen class or
 *   method, i.e. "what leads into this" - the entry channels through which it is reached. A domain
 *   command can never be a backward flow target, so commands are not offered for it.</li>
 * </ul>
 * Forward and backward flows can be combined; the diagram then shows the types reached by either.
 * <p>
 * This requires the result of a static analysis of the domain classes ({@code DomainCalls}) to have
 * been uploaded alongside the domain mirror; the filter is unavailable otherwise.
 */
@Slf4j
public class DiagramFlowFilterComponent extends Div {

    private static final String FLOW_METHOD_SEPARATOR = "#";

    /**
     * The direction a flow added via this component is followed in.
     */
    public enum FlowDirection {
        FORWARD("Forward (what it leads to)"),
        BACKWARD("Backward (what leads into it)");

        private final String label;

        FlowDirection(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private final SessionStorage sessionStorage;
    private final DiagramService diagramService;
    private Diagram currentDiagram;
    private FlowDirection selectedDirection = FlowDirection.FORWARD;

    private List<DomainTypeMirror> selectableTypes;
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

        boolean domainCallsAvailable = sessionStorage.getDomainCalls(currentDiagram.getProject().getId()).isPresent();

        Details flowDetails = new Details(domainCallsAvailable ? "Flow filter" : "Flow filter (unavailable)");
        flowDetails.setWidthFull();
        flowDetails.setOpened(sessionStorage.isFlowFilterOpen());
        flowDetails.addOpenedChangeListener(e -> sessionStorage.setFlowFilterOpen(e.isOpened()));

        if (!domainCallsAvailable) {
            addFlowFilteringUnavailableContent(flowDetails);
            add(flowDetails);
            return;
        }

        List<DomainTypeMirror> domainTypeMirrors = sessionStorage.getAllDomainTypeMirrorsWithoutEnumsAndIds(
            currentDiagram.getProject().getId());
        selectableTypes = DomainModelUtils.filterConcreteMirrorsInterfaceAvailable(currentDiagram, domainTypeMirrors);

        RadioButtonGroup<FlowDirection> directionRadioGroup = new RadioButtonGroup<>("Direction");
        directionRadioGroup.setItems(FlowDirection.values());
        directionRadioGroup.setItemLabelGenerator(FlowDirection::getLabel);
        directionRadioGroup.setValue(selectedDirection);
        directionRadioGroup.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                selectedDirection = e.getValue();
                updateClassComboBoxForSelectedDirection();
            }
        });

        classComboBox = new ComboBox<>("Class");
        classComboBox.setWidthFull();
        classComboBox.setItemLabelGenerator(DomainModelUtils::nameWithStereoType);

        methodComboBox = new ComboBox<>("Method (optional, restricts to that method's flow)");
        methodComboBox.setWidthFull();
        methodComboBox.setItemLabelGenerator(this::methodLabel);
        methodComboBox.setEnabled(false);

        classComboBox.addValueChangeListener(e -> updateMethodComboBoxForSelectedClass(e.getValue()));
        updateClassComboBoxForSelectedDirection();

        Button addButton = new Button("Add flow", e -> addFlowPoint());
        addButton.setWidthFull();

        HorizontalLayout selectionLayout = new HorizontalLayout(classComboBox, methodComboBox);
        selectionLayout.setWidthFull();
        selectionLayout.setFlexGrow(1, classComboBox, methodComboBox);

        flowDetails.add(directionRadioGroup, selectionLayout, addButton);

        DomainModelVisibility visibility = currentDiagram.getDomainModelVisibility();
        Set<String> activeFlowsFrom = visibility.getIncludeFlowsFrom();
        if (!activeFlowsFrom.isEmpty()) {
            flowDetails.add(createActiveFlowsComboBox("Active flow filters:", activeFlowsFrom,
                selected -> applyFlows(selected, visibility.getIncludeFlowsTo())));
        }
        Set<String> activeFlowsTo = visibility.getIncludeFlowsTo();
        if (!activeFlowsTo.isEmpty()) {
            flowDetails.add(createActiveFlowsComboBox("Active backward flow filters:", activeFlowsTo,
                selected -> applyFlows(visibility.getIncludeFlowsFrom(), selected)));
        }

        add(flowDetails);
        log.debug("refreshDetails DiagramFlowFilterComponent finished");
    }

    /**
     * Without an uploaded static analysis result, flow filtering is disabled: no flows can be added,
     * and flows configured earlier are ignored when the diagram is rendered. They are kept though -
     * shown here read-only - and apply again as soon as an analysis result is uploaded.
     */
    private void addFlowFilteringUnavailableContent(Details flowDetails) {
        flowDetails.add(new Paragraph(
            "Flow filtering is disabled, since no result of a static code analysis (DomainCalls) was uploaded "
                + "alongside the domain mirror. Upload it (e.g. via the DLC build plugin with "
                + "'runStaticAnalysis = true') to restrict this diagram to one or more flows."));

        DomainModelVisibility visibility = currentDiagram.getDomainModelVisibility();
        Set<String> inactiveFlows = new LinkedHashSet<>(visibility.getIncludeFlowsFrom());
        inactiveFlows.addAll(visibility.getIncludeFlowsTo());
        if (!inactiveFlows.isEmpty()) {
            MultiSelectComboBox<String> inactiveFlowsComboBox = new MultiSelectComboBox<>("Inactive flow filters:");
            inactiveFlowsComboBox.setWidthFull();
            inactiveFlowsComboBox.setItems(inactiveFlows);
            inactiveFlowsComboBox.setItemLabelGenerator(flowPoint -> flowPointLabel(flowPoint)
                + (visibility.getIncludeFlowsTo().contains(flowPoint) ? " (backward)" : ""));
            inactiveFlowsComboBox.select(inactiveFlows);
            inactiveFlowsComboBox.setReadOnly(true);
            inactiveFlowsComboBox.setHelperText("Not applied to the diagram until a static analysis result is uploaded.");
            flowDetails.add(inactiveFlowsComboBox);
        }
    }

    private MultiSelectComboBox<String> createActiveFlowsComboBox(String label,
                                                                  Set<String> activeFlows,
                                                                  Consumer<Set<String>> onChange) {
        MultiSelectComboBox<String> activeFlowsComboBox = new MultiSelectComboBox<>(label);
        activeFlowsComboBox.setWidthFull();
        activeFlowsComboBox.setItems(activeFlows);
        activeFlowsComboBox.setItemLabelGenerator(this::flowPointLabel);
        activeFlowsComboBox.select(activeFlows);
        activeFlowsComboBox.addValueChangeListener(e -> onChange.accept(new LinkedHashSet<>(e.getValue())));
        return activeFlowsComboBox;
    }

    /**
     * A domain command cannot be a backward flow target (nothing in the analyzed data leads into a
     * command), so commands are only offered for forward flows.
     */
    private void updateClassComboBoxForSelectedDirection() {
        DomainTypeMirror previouslySelected = classComboBox.getValue();
        List<DomainTypeMirror> items = selectedDirection == FlowDirection.BACKWARD
            ? selectableTypes.stream().filter(type -> !(type instanceof DomainCommandMirror)).toList()
            : selectableTypes;
        classComboBox.setItems(items);
        classComboBox.setHelperText(selectedDirection == FlowDirection.BACKWARD
            ? "Domain commands cannot be a backward flow target."
            : null);
        if (previouslySelected != null && items.contains(previouslySelected)) {
            classComboBox.setValue(previouslySelected);
        } else {
            updateMethodComboBoxForSelectedClass(null);
        }
    }

    private void updateMethodComboBoxForSelectedClass(DomainTypeMirror selectedType) {
        methodComboBox.clear();
        if (selectedType == null || selectedType instanceof DomainCommandMirror || selectedType instanceof DomainEventMirror) {
            methodComboBox.setItems(List.of());
            methodComboBox.setEnabled(false);
            methodComboBox.setHelperText(selectedType == null ? null : wholeTypeHelperText(selectedType));
            return;
        }

        List<MethodMirror> methods = selectedType.getMethods().stream()
            .sorted(Comparator.comparing(MethodMirror::getName))
            .toList();
        methodComboBox.setHelperText("Leave empty to include the flows of all methods.");
        methodComboBox.setItems(methods);
        methodComboBox.setEnabled(true);
    }

    private String wholeTypeHelperText(DomainTypeMirror selectedType) {
        if (selectedType instanceof DomainCommandMirror) {
            return "Starts the flow triggered by this command.";
        }
        return selectedDirection == FlowDirection.BACKWARD
            ? "Includes everything leading into the publishing of this event."
            : "Starts the flow triggered by this event.";
    }

    private void addFlowPoint() {
        DomainTypeMirror selectedType = classComboBox.getValue();
        if (selectedType == null) {
            return;
        }
        MethodMirror selectedMethod = methodComboBox.isEnabled() ? methodComboBox.getValue() : null;
        String flowPoint = selectedMethod == null
            ? selectedType.getTypeName()
            : selectedType.getTypeName() + FLOW_METHOD_SEPARATOR + selectedMethod.getName();

        DomainModelVisibility visibility = currentDiagram.getDomainModelVisibility();
        Set<String> updatedFlowsFrom = new LinkedHashSet<>(visibility.getIncludeFlowsFrom());
        Set<String> updatedFlowsTo = new LinkedHashSet<>(visibility.getIncludeFlowsTo());
        if (selectedDirection == FlowDirection.BACKWARD) {
            updatedFlowsTo.add(flowPoint);
        } else {
            updatedFlowsFrom.add(flowPoint);
        }
        applyFlows(updatedFlowsFrom, updatedFlowsTo);
    }

    private void applyFlows(Set<String> newFlowsFrom, Set<String> newFlowsTo) {
        DomainModelVisibility newVisibility = currentDiagram.getDomainModelVisibility()
            .replaceIncludeFlowsFrom(new LinkedHashSet<>(newFlowsFrom))
            .replaceIncludeFlowsTo(new LinkedHashSet<>(newFlowsTo));
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

    private String flowPointLabel(String flowPoint) {
        int separatorIndex = flowPoint.indexOf(FLOW_METHOD_SEPARATOR);
        if (separatorIndex < 0) {
            return shortTypeName(flowPoint);
        }
        return shortTypeName(flowPoint.substring(0, separatorIndex))
            + FLOW_METHOD_SEPARATOR + flowPoint.substring(separatorIndex + FLOW_METHOD_SEPARATOR.length());
    }

    private String shortTypeName(String fullyQualifiedTypeName) {
        return fullyQualifiedTypeName.substring(fullyQualifiedTypeName.lastIndexOf('.') + 1);
    }
}
