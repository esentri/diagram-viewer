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
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.IntegerField;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.service.BoundedContext;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.util.DomainModelUtils;
import io.domainlifecycles.diagramviewer.webapp.components.various.selects.PackageMultiSelectComboBox;
import io.domainlifecycles.diagramviewer.webapp.rendering.BackgroundDiagramRendering;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.mirror.api.DomainType;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DiagramFilterComponent extends Div {

    static final String INGOING_DEPTH_ID = "include-connected-ingoing-depth";
    static final String OUTGOING_DEPTH_ID = "include-connected-outgoing-depth";

    private final SessionStorage sessionStorage;
    private final DiagramService diagramService;
    private Diagram currentDiagram;

    private MultiSelectComboBox<DomainTypeMirror> comboBoxConnected;
    private MultiSelectComboBox<DomainTypeMirror> comboBoxConnectedIngoing;
    private MultiSelectComboBox<DomainTypeMirror> comboBoxConnectedOutgoing;
    private MultiSelectComboBox<DomainTypeMirror> comboBoxConnectedExcludeIngoing;
    private MultiSelectComboBox<DomainTypeMirror> comboBoxConnectedExcludeOutgoing;
    private MultiSelectComboBox<DomainTypeMirror> comboBoxInvisibleDomainObjects;
    private MultiSelectComboBox<DomainTypeMirror> comboBoxInlinedValueObjects;
    private IntegerField connectedIngoingDepthField;
    private IntegerField connectedOutgoingDepthField;

    public DiagramFilterComponent(
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
        if(this.currentDiagram != null) {
            var domainTypeMirrors = sessionStorage.getAllDomainTypeMirrorsWithoutEnumsAndIds(currentDiagram.getProject().getId());
            log.debug("refreshDetails DiagramVisibilityComponent started");
            UUID projectId = currentDiagram.getProject().getId();
            if (sessionStorage.hasDeclaredBoundedContexts(projectId)) {
                add(createBoundedContextDetails(sessionStorage.getBoundedContexts(projectId)));
            }

            Details packageDetails = new Details("Explicitly included packages");
            packageDetails.setWidthFull();
            packageDetails.setOpened(sessionStorage.isPackageFilterOpen());

            MultiSelectComboBox<String> packageMultiSelectComboBox =
                    new PackageMultiSelectComboBox(domainTypeMirrors, currentDiagram);
            packageMultiSelectComboBox.addValueChangeListener(e -> {
                currentDiagram.setDomainModelVisibility(currentDiagram.getDomainModelVisibility().replaceExplicitlyIncludedPackagesNames(e.getValue()));
                var newDiagram = BackgroundDiagramRendering.updateModelAndImage(this, diagramService, currentDiagram);
            });

            packageDetails.add(packageMultiSelectComboBox);
            packageDetails.addOpenedChangeListener(e -> sessionStorage.setPackageFilterOpen(e.isOpened()));

            Details advancedFilterDetails = new Details("Advanced view filters");
            advancedFilterDetails.setWidthFull();
            advancedFilterDetails.setOpened(sessionStorage.isAdvancedTrimmingOpen());
            advancedFilterDetails.addOpenedChangeListener(e -> sessionStorage.setAdvancedTrimmingOpen(e.isOpened()));

            List<DomainTypeMirror> items = DomainModelUtils.filterConcreteMirrorsInterfaceAvailable(currentDiagram, domainTypeMirrors);

            List<DomainTypeMirror> valueObjects = filterValueObjectMirrorAvailable(currentDiagram, domainTypeMirrors);

            comboBoxConnected = createAndConfigureComboBox(
                    ComboBoxVisibilityType.INCLUDE_CONNECTED,
                    items
            );
            advancedFilterDetails.add(comboBoxConnected);

            comboBoxConnectedIngoing = createAndConfigureComboBox(
                    ComboBoxVisibilityType.INCLUDE_CONNECTED_INGOING,
                    items
            );
            advancedFilterDetails.add(comboBoxConnectedIngoing);
            connectedIngoingDepthField = createAndConfigureDepthField(INGOING_DEPTH_ID, "what leads to them",
                comboBoxConnectedIngoing, currentDiagram.getDomainModelVisibility().getIncludeConnectedToIngoingDepth());
            advancedFilterDetails.add(connectedIngoingDepthField);

            comboBoxConnectedOutgoing = createAndConfigureComboBox(
                    ComboBoxVisibilityType.INCLUDE_CONNECTED_OUTGOING,
                    items
            );
            advancedFilterDetails.add(comboBoxConnectedOutgoing);
            connectedOutgoingDepthField = createAndConfigureDepthField(OUTGOING_DEPTH_ID, "what they lead to",
                comboBoxConnectedOutgoing, currentDiagram.getDomainModelVisibility().getIncludeConnectedToOutgoingDepth());
            advancedFilterDetails.add(connectedOutgoingDepthField);

            comboBoxConnectedExcludeIngoing = createAndConfigureComboBox(
                    ComboBoxVisibilityType.EXCLUDE_CONNECTED_INGOING,
                    items
            );
            advancedFilterDetails.add(comboBoxConnectedExcludeIngoing);

            comboBoxConnectedExcludeOutgoing = createAndConfigureComboBox(
                    ComboBoxVisibilityType.EXCLUDE_CONNECTED_OUTGOING,
                    items
            );
            advancedFilterDetails.add(comboBoxConnectedExcludeOutgoing);

            comboBoxInvisibleDomainObjects = createAndConfigureComboBox(
                    ComboBoxVisibilityType.INVISIBLE,
                    items
            );
            advancedFilterDetails.add(comboBoxInvisibleDomainObjects);
            comboBoxInlinedValueObjects = createAndConfigureComboBox(
                    ComboBoxVisibilityType.INLINED_VALUE_OBJECTS,
                    valueObjects
            );
            advancedFilterDetails.add(comboBoxInlinedValueObjects);

            add(packageDetails);
            add(advancedFilterDetails);
            log.debug("refreshDetails DiagramVisibilityComponent finished");
        }
    }

    private MultiSelectComboBox<DomainTypeMirror> createAndConfigureComboBox(
            ComboBoxVisibilityType comboBoxVisibilityType,
            List<DomainTypeMirror> items
    ) {
        Set<String> unavailable = unavailableClassNames(comboBoxVisibilityType, currentDiagram.getDomainModelVisibility());
        var selectedClassNames = switch (comboBoxVisibilityType){
            case INCLUDE_CONNECTED -> currentDiagram.getDomainModelVisibility().getIncludeConnectedToClassNames();
            case INCLUDE_CONNECTED_INGOING ->  currentDiagram.getDomainModelVisibility().getIncludeConnectedToIngoingClassNames();
            case INCLUDE_CONNECTED_OUTGOING ->   currentDiagram.getDomainModelVisibility().getIncludeConnectedToOutgoingClassNames();
            case EXCLUDE_CONNECTED_INGOING ->   currentDiagram.getDomainModelVisibility().getExcludeConnectedToIngoingClassNames();
            case EXCLUDE_CONNECTED_OUTGOING ->   currentDiagram.getDomainModelVisibility().getExcludeConnectedToOutgoingClassNames();
            case INVISIBLE -> currentDiagram.getDomainModelVisibility().getBlacklistedClassNames();
            case INLINED_VALUE_OBJECTS -> currentDiagram.getDomainModelVisibility().getInlinedValueObjects();
        };
        unavailable.removeAll(selectedClassNames);

        var itemsRemovedUnavailable = items.stream()
                .filter(it -> !unavailable.contains(it.getTypeName()))
                .toList();

        MultiSelectComboBox<DomainTypeMirror> multiSelectComboBox = new MultiSelectComboBox<>(comboBoxVisibilityType.label);
        multiSelectComboBox.setWidthFull();
        multiSelectComboBox.setItems(itemsRemovedUnavailable);
        multiSelectComboBox.setItemLabelGenerator(DomainModelUtils::nameWithStereoType);

        var selected = selected(items, selectedClassNames);
        multiSelectComboBox.select(selected);
        multiSelectComboBox.addValueChangeListener(e -> regenerateDiagram(
            currentDiagram,
            comboBoxConnected.getSelectedItems(),
            comboBoxConnectedIngoing.getSelectedItems(),
            comboBoxConnectedOutgoing.getSelectedItems(),
            comboBoxConnectedExcludeIngoing.getSelectedItems(),
            comboBoxConnectedExcludeOutgoing.getSelectedItems(),
            comboBoxInvisibleDomainObjects.getSelectedItems(),
            comboBoxInlinedValueObjects.getSelectedItems()
        ));

        return multiSelectComboBox;
    }

    /**
     * The classes not offered by a combo box, as selected in a connection filter that excludes each other with it: a
     * class may be followed in both directions (ingoing and outgoing, included or excluded), but must not be included
     * and excluded at once, nor be included with all its connections and in one direction. The inlined value objects
     * leave out all classes of the connection filters, the invisible objects none.
     */
    static Set<String> unavailableClassNames(ComboBoxVisibilityType comboBoxVisibilityType, DomainModelVisibility visibility) {
        Set<String> unavailable = new HashSet<>();
        Set<String> includedAll = visibility.getIncludeConnectedToClassNames();
        Set<String> includedDirected = new HashSet<>(visibility.getIncludeConnectedToIngoingClassNames());
        includedDirected.addAll(visibility.getIncludeConnectedToOutgoingClassNames());
        Set<String> excludedDirected = new HashSet<>(visibility.getExcludeConnectedToIngoingClassNames());
        excludedDirected.addAll(visibility.getExcludeConnectedToOutgoingClassNames());
        switch (comboBoxVisibilityType) {
            case INCLUDE_CONNECTED -> {
                unavailable.addAll(includedDirected);
                unavailable.addAll(excludedDirected);
            }
            case INCLUDE_CONNECTED_INGOING, INCLUDE_CONNECTED_OUTGOING -> {
                unavailable.addAll(includedAll);
                unavailable.addAll(excludedDirected);
            }
            case EXCLUDE_CONNECTED_INGOING, EXCLUDE_CONNECTED_OUTGOING -> {
                unavailable.addAll(includedAll);
                unavailable.addAll(includedDirected);
            }
            case INLINED_VALUE_OBJECTS -> {
                unavailable.addAll(includedAll);
                unavailable.addAll(includedDirected);
                unavailable.addAll(excludedDirected);
            }
            case INVISIBLE -> {
                // any class may be hidden
            }
        }
        return unavailable;
    }

    /**
     * The depth up to which the connections of the classes selected in the combo box are followed: {@code 0} - also
     * shown for an empty field - follows the complete path. Only enabled while classes are selected.
     */
    private IntegerField createAndConfigureDepthField(String id, String followedConnections,
                                                      MultiSelectComboBox<DomainTypeMirror> comboBox, int depth) {
        IntegerField depthField = new IntegerField("Depth");
        depthField.setId(id);
        depthField.setWidthFull();
        depthField.setMin(0);
        depthField.setStepButtonsVisible(true);
        depthField.setHelperText("Up to how many steps " + followedConnections + " is shown, 0 shows the complete path");
        depthField.setValue(Math.max(depth, 0));
        depthField.setEnabled(!comboBox.getSelectedItems().isEmpty());
        comboBox.addValueChangeListener(e -> depthField.setEnabled(!e.getValue().isEmpty()));
        depthField.addValueChangeListener(e -> regenerateDiagramWithDepths());
        return depthField;
    }

    private void regenerateDiagramWithDepths() {
        currentDiagram.setDomainModelVisibility(currentDiagram.getDomainModelVisibility().replaceIncludeConnectedDepths(
            depth(connectedIngoingDepthField), depth(connectedOutgoingDepthField)));
        BackgroundDiagramRendering.updateModelAndImage(this, diagramService, currentDiagram);
    }

    private static int depth(IntegerField depthField) {
        return depthField.getValue() == null ? 0 : Math.max(depthField.getValue(), 0);
    }

    private DomainTypeMirror[] selected(List<DomainTypeMirror> typeMirrors, Set<String> typeNames){
        return typeMirrors.stream()
                .filter(dt -> typeNames.contains(dt.getTypeName()))
                .toArray(DomainTypeMirror[]::new);
    }

    private void regenerateDiagram(
            Diagram diagram,
            Set<DomainTypeMirror> domainTypeMirrorsConnected,
            Set<DomainTypeMirror> domainTypeMirrorsConnectedIngoing,
            Set<DomainTypeMirror> domainTypeMirrorsConnectedOutgoing,
            Set<DomainTypeMirror> domainTypeMirrorsExcludedIngoing,
            Set<DomainTypeMirror> domainTypeMirrorsExcludedOutgoing,
            Set<DomainTypeMirror> invisibleDomainObjects,
            Set<DomainTypeMirror> inlinedValueObjects
    ) {
        DomainModelVisibility newVisibility = diagram.getDomainModelVisibility();
        newVisibility = newVisibility.replaceIncludeConnectedToClassNames(
            domainTypeMirrorsConnected.stream().map(DomainTypeMirror::getTypeName).collect(Collectors.toSet())
        );
        newVisibility = newVisibility.replaceIncludeConnectedToIngoingClassNames(
            domainTypeMirrorsConnectedIngoing.stream().map(DomainTypeMirror::getTypeName).collect(Collectors.toSet())
        );
        newVisibility = newVisibility.replaceIncludeConnectedToOutgoingClassNames(
            domainTypeMirrorsConnectedOutgoing.stream().map(DomainTypeMirror::getTypeName).collect(Collectors.toSet())
        );
        newVisibility = newVisibility.replaceExcludeConnectedToIngoingClassNames(
            domainTypeMirrorsExcludedIngoing.stream().map(DomainTypeMirror::getTypeName).collect(Collectors.toSet())
        );
        newVisibility = newVisibility.replaceExcludeConnectedToOutgoingClassNames(
            domainTypeMirrorsExcludedOutgoing.stream().map(DomainTypeMirror::getTypeName).collect(Collectors.toSet())
        );
        newVisibility = newVisibility.replaceBlacklistedClassNames(
                invisibleDomainObjects.stream().map(DomainTypeMirror::getTypeName).collect(Collectors.toSet())
        );
        newVisibility = newVisibility.replaceInlinedValueObjects(
                inlinedValueObjects.stream().map(DomainTypeMirror::getTypeName).collect(Collectors.toSet())
        );

        diagram.setDomainModelVisibility(newVisibility);
        diagram = BackgroundDiagramRendering.updateModelAndImage(this, diagramService, diagram);
    }

    /**
     * Restricts the diagram to Bounded Contexts, labelled by their name or, if they have none, their package. Only
     * offered for domain models declaring Bounded Contexts; combined with the package filter, see
     * {@link DomainModelVisibility#getEffectiveIncludedPackages()}.
     */
    private Details createBoundedContextDetails(List<BoundedContext> boundedContexts) {
        Details boundedContextDetails = new Details("Bounded Contexts");
        boundedContextDetails.setWidthFull();
        boundedContextDetails.setOpened(true);

        MultiSelectComboBox<BoundedContext> boundedContextComboBox = new MultiSelectComboBox<>();
        boundedContextComboBox.setId("bounded-context-filter");
        boundedContextComboBox.setWidthFull();
        boundedContextComboBox.setPlaceholder("All Bounded Contexts");
        boundedContextComboBox.setItems(boundedContexts);
        boundedContextComboBox.setItemLabelGenerator(BoundedContext::label);
        Set<String> includedPackages = currentDiagram.getDomainModelVisibility().getIncludedBoundedContextPackages();
        boundedContextComboBox.setValue(boundedContexts.stream()
            .filter(boundedContext -> includedPackages.contains(boundedContext.packageName()))
            .collect(Collectors.toSet()));
        boundedContextComboBox.addValueChangeListener(e -> {
            currentDiagram.setDomainModelVisibility(currentDiagram.getDomainModelVisibility()
                .replaceIncludedBoundedContextPackages(e.getValue().stream()
                    .map(BoundedContext::packageName)
                    .collect(Collectors.toSet())));
            currentDiagram = BackgroundDiagramRendering.updateModelAndImage(this, diagramService, currentDiagram);
        });

        boundedContextDetails.add(boundedContextComboBox);
        return boundedContextDetails;
    }

    private List<DomainTypeMirror> filterValueObjectMirrorAvailable(Diagram diagram, List<DomainTypeMirror> domainTypeMirrors) {
        List<DomainTypeMirror> domainTypeMirrorsFiltered = new ArrayList<>();

        if (domainTypeMirrors != null && !domainTypeMirrors.isEmpty()) {
            domainTypeMirrorsFiltered.addAll(
                    domainTypeMirrors
                            .stream()
                            .filter(m -> !m.getTypeName().startsWith(DomainModelUtils.DOMAINLIFECYCLES_PACKAGE_NAME))
                            .filter(m ->
                                  m.getDomainType().equals(DomainType.VALUE_OBJECT)
                            )
                            .toList()
            );

        }

        return domainTypeMirrorsFiltered.stream().sorted(
                Comparator.comparing(DomainTypeMirror::getTypeName)).collect(Collectors.toList());
    }



    enum ComboBoxVisibilityType {
        INCLUDE_CONNECTED("Include Connections to:"),
        INCLUDE_CONNECTED_INGOING ("Include ingoing connections to:"),
        INCLUDE_CONNECTED_OUTGOING("Include outgoing connections from:"),
        EXCLUDE_CONNECTED_INGOING("Exclude ingoing connections to:"),
        EXCLUDE_CONNECTED_OUTGOING("Exclude outgoing connections from:"),
        INVISIBLE("Invisible Objects:"),
        INLINED_VALUE_OBJECTS("Inlined ValueObjects:");

        final String label;

        ComboBoxVisibilityType(String label) {
            this.label = label;
        }
    }

}
