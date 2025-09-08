package io.domainlifecycles.diagramviewer.webapp.components.various.filtering;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.components.various.selects.PackageMultiSelectComboBox;
import io.domainlifecycles.diagramviewer.webapp.events.global.DiagramForDiagramViewChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.events.global.GlobalUIEventBus;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.mirror.api.DomainType;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class DiagramFilterComponent extends Div {

    public static final String DOMAINLIFECYCLES_PACKAGE_NAME = "io.domainlifecycles";

    private final SessionStorage sessionStorage;
    private final DiagramService diagramService;
    private Diagram currentDiagram;
    private final GlobalUIEventBus globalUIEventBus;



    private MultiSelectComboBox<DomainTypeMirror> comboBoxConnected;
    private MultiSelectComboBox<DomainTypeMirror> comboBoxConnectedIngoing;
    private MultiSelectComboBox<DomainTypeMirror> comboBoxConnectedOutgoing;
    private MultiSelectComboBox<DomainTypeMirror> comboBoxConnectedExcludeIngoing;
    private MultiSelectComboBox<DomainTypeMirror> comboBoxConnectedExcludeOutgoing;
    private MultiSelectComboBox<DomainTypeMirror> comboBoxInvisibleDomainObjects;

    public DiagramFilterComponent(
            GlobalUIEventBus globalUIEventBus,
            SessionStorage sessionStorage,
            DiagramService diagramService) {
        this.sessionStorage = sessionStorage;
        this.diagramService = diagramService;
        this.globalUIEventBus = globalUIEventBus;
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
            Details packageDetails = new Details("Explicitly included packages");
            packageDetails.setWidthFull();
            packageDetails.setOpened(sessionStorage.isPackageFilterOpen());

            MultiSelectComboBox<String> packageMultiSelectComboBox =
                    new PackageMultiSelectComboBox(domainTypeMirrors, currentDiagram);
            packageMultiSelectComboBox.addValueChangeListener(e -> {
                currentDiagram.setDomainModelVisibility(currentDiagram.getDomainModelVisibility().replaceExplicitlyIncludedPackagesNames(e.getValue()));
                var newDiagram = diagramService.updateModelAndImage(currentDiagram);
                globalUIEventBus.fireEvent(new DiagramForDiagramViewChangedEvent(newDiagram, this));
            });

            packageDetails.add(packageMultiSelectComboBox);
            packageDetails.addOpenedChangeListener(e -> sessionStorage.setPackageFilterOpen(e.isOpened()));

            Details advancedFilterDetails = new Details("Advanced diagram trimming");
            advancedFilterDetails.setWidthFull();
            advancedFilterDetails.setOpened(sessionStorage.isAdvancedTrimmingOpen());
            advancedFilterDetails.addOpenedChangeListener(e -> sessionStorage.setAdvancedTrimmingOpen(e.isOpened()));

            List<DomainTypeMirror> items = filterConcreteMirrorsInterfaceAvailable(currentDiagram, domainTypeMirrors);

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

            comboBoxConnectedOutgoing = createAndConfigureComboBox(
                    ComboBoxVisibilityType.INCLUDE_CONNECTED_OUTGOING,
                    items
            );
            advancedFilterDetails.add(comboBoxConnectedOutgoing);

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

            add(packageDetails);
            add(advancedFilterDetails);
            log.debug("refreshDetails DiagramVisibilityComponent finished");
        }
    }

    private MultiSelectComboBox<DomainTypeMirror> createAndConfigureComboBox(
            ComboBoxVisibilityType comboBoxVisibilityType,
            List<DomainTypeMirror> items
    ) {
        Set<String> selectedAnyWhere = new HashSet<>();
        if(!comboBoxVisibilityType.equals(ComboBoxVisibilityType.INVISIBLE)) {
            selectedAnyWhere.addAll(currentDiagram.getDomainModelVisibility().getIncludeConnectedToClassNames());
            selectedAnyWhere.addAll(currentDiagram.getDomainModelVisibility().getIncludeConnectedToIngoingClassNames());
            selectedAnyWhere.addAll(currentDiagram.getDomainModelVisibility().getIncludeConnectedToOutgoingClassNames());
            selectedAnyWhere.addAll(currentDiagram.getDomainModelVisibility().getExcludeConnectedToIngoingClassNames());
            selectedAnyWhere.addAll(currentDiagram.getDomainModelVisibility().getExcludeConnectedToOutgoingClassNames());

        }
        Set<String> unavailable = new HashSet<>(selectedAnyWhere);
        var selectedClassNames = switch (comboBoxVisibilityType){
            case INCLUDE_CONNECTED -> currentDiagram.getDomainModelVisibility().getIncludeConnectedToClassNames();
            case INCLUDE_CONNECTED_INGOING ->  currentDiagram.getDomainModelVisibility().getIncludeConnectedToIngoingClassNames();
            case INCLUDE_CONNECTED_OUTGOING ->   currentDiagram.getDomainModelVisibility().getIncludeConnectedToOutgoingClassNames();
            case EXCLUDE_CONNECTED_INGOING ->   currentDiagram.getDomainModelVisibility().getExcludeConnectedToIngoingClassNames();
            case EXCLUDE_CONNECTED_OUTGOING ->   currentDiagram.getDomainModelVisibility().getExcludeConnectedToOutgoingClassNames();
            case INVISIBLE -> currentDiagram.getDomainModelVisibility().getBlacklistedClassNames();
        };
        unavailable.removeAll(selectedClassNames);

        var itemsRemovedUnavailable = items.stream()
                .filter(it -> !unavailable.contains(it.getTypeName()))
                .toList();

        MultiSelectComboBox<DomainTypeMirror> multiSelectComboBox = new MultiSelectComboBox<>(comboBoxVisibilityType.label);
        multiSelectComboBox.setWidthFull();
        multiSelectComboBox.setItems(itemsRemovedUnavailable);
        multiSelectComboBox.setItemLabelGenerator(this::name);

        var selected = selected(items, selectedClassNames);
        multiSelectComboBox.select(selected);
        multiSelectComboBox.addValueChangeListener(e -> regenerateDiagram(
            currentDiagram,
            comboBoxConnected.getSelectedItems(),
            comboBoxConnectedIngoing.getSelectedItems(),
            comboBoxConnectedOutgoing.getSelectedItems(),
            comboBoxConnectedExcludeIngoing.getSelectedItems(),
            comboBoxConnectedExcludeOutgoing.getSelectedItems(),
            comboBoxInvisibleDomainObjects.getSelectedItems()
        ));

        return multiSelectComboBox;
    }

    private String name(DomainTypeMirror mirror) {
        return mirror.getTypeName().substring(mirror.getTypeName().lastIndexOf('.') + 1)
                + " <" + translateDomainType(mirror.getDomainType())+">";
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
            Set<DomainTypeMirror> invisibleDomainObjects
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

        diagram.setDomainModelVisibility(newVisibility);
        diagram = diagramService.updateModelAndImage(diagram);
        globalUIEventBus.fireEvent(new DiagramForDiagramViewChangedEvent(diagram, this));
    }

    private List<DomainTypeMirror> filterConcreteMirrorsInterfaceAvailable(Diagram diagram, List<DomainTypeMirror> domainTypeMirrors) {
        List<DomainTypeMirror> domainTypeMirrorsFiltered = new ArrayList<>();

        if (domainTypeMirrors != null && domainTypeMirrors.size() > 0) {
            List<String> mirroredTypeNames = domainTypeMirrors.stream()
                .map(DomainTypeMirror::getTypeName)
                .filter(typeName -> !typeName.startsWith(DOMAINLIFECYCLES_PACKAGE_NAME)).toList();
            domainTypeMirrorsFiltered.addAll(
                domainTypeMirrors
                    .stream()
                    .filter(type ->
                        diagram.getDomainModelVisibility().getExplicitlyIncludedPackagesNames().isEmpty()
                            || diagram.getDomainModelVisibility().getExplicitlyIncludedPackagesNames().stream()
                            .anyMatch(p -> type.getTypeName().startsWith(p)))
                    .filter(m -> !m.getTypeName().startsWith(DOMAINLIFECYCLES_PACKAGE_NAME))
                    .filter(m ->
                        !m.getDomainType().equals(DomainType.ENUM) &&
                                !m.getDomainType().equals(DomainType.IDENTITY)
                        && !m.getDomainType().equals(DomainType.VALUE_OBJECT)
                    )
                    .toList()
            );

            if(!diagram.getDiagramStylingConfiguration().isShowAllInheritanceStructures()){
                for (DomainTypeMirror mirror : domainTypeMirrors) {
                    switch (mirror.getDomainType()) {
                        case SERVICE_KIND, OUTBOUND_SERVICE, APPLICATION_SERVICE, DOMAIN_SERVICE, REPOSITORY, QUERY_HANDLER -> {
                            for (String interfaceTypeName : mirror.getAllInterfaceTypeNames()) {
                                if (!interfaceTypeName.startsWith(DOMAINLIFECYCLES_PACKAGE_NAME) && !diagram.getDiagramStylingConfiguration().isShowInheritanceStructuresForServiceKinds()) {
                                    if (mirroredTypeNames.contains(interfaceTypeName)) {
                                        domainTypeMirrorsFiltered.remove(mirror);
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

        return domainTypeMirrorsFiltered.stream().sorted(
                Comparator.comparing(DomainTypeMirror::getTypeName)).collect(Collectors.toList());
    }

    private String translateDomainType(DomainType domainType) {
        return switch (domainType) {
            case ENUM -> "Enum";
            case AGGREGATE_ROOT -> "AggregateRoot";
            case ENTITY -> "Entity";
            case IDENTITY -> "Identity";
            case READ_MODEL -> "ReadModel";
            case REPOSITORY -> "Repository";
            case DOMAIN_EVENT -> "DomainEvent";
            case SERVICE_KIND -> "Service";
            case VALUE_OBJECT -> "ValueObject";
            case QUERY_HANDLER -> "QueryHandler";
            case DOMAIN_COMMAND -> "DomainCommand";
            case OUTBOUND_SERVICE -> "OutboundService";
            case DOMAIN_SERVICE -> "DomainService";
            case APPLICATION_SERVICE -> "ApplicationService";
            default -> "Object";
        };
    }

    private enum ComboBoxVisibilityType {
        INCLUDE_CONNECTED("Include Connections to:"),
        INCLUDE_CONNECTED_INGOING ("Include ingoing connections to:"),
        INCLUDE_CONNECTED_OUTGOING("Include outgoing connections from:"),
        EXCLUDE_CONNECTED_INGOING("Exclude ingoing connections to:"),
        EXCLUDE_CONNECTED_OUTGOING("Exclude outgoing connections from:"),
        INVISIBLE("Invisible domain objects:");

        final String label;

        ComboBoxVisibilityType(String label) {
            this.label = label;
        }
    }

}
