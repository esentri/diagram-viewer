package io.domainlifecycles.diagramviewer.webapp.components.various.filtering;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.components.various.selects.PackageMultiSelectComboBox;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramStylingChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.mirror.api.DomainType;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DiagramFilterComponent extends Div {

    public static final String DOMAINLIFECYCLES_PACKAGE_NAME = "io.domainlifecycles";

    private final SessionStorage sessionStorage;
    private final Diagram diagram;
    private final Project project;
    private final DiagramService diagramService;
    private final List<DomainTypeMirror> domainTypeMirrors;

    private MultiSelectComboBox<DomainTypeMirror> comboBoxConnected;
    private MultiSelectComboBox<DomainTypeMirror> comboBoxConnectedIngoing;
    private MultiSelectComboBox<DomainTypeMirror> comboBoxConnectedOutgoing;
    private MultiSelectComboBox<DomainTypeMirror> comboBoxConnectedExcludeIngoing;
    private MultiSelectComboBox<DomainTypeMirror> comboBoxConnectedExcludeOutgoing;
    private MultiSelectComboBox<DomainTypeMirror> comboBoxInvisibleDomainObjects;

    public DiagramFilterComponent(
            SessionStorage sessionStorage,
            Diagram diagram,
            Project project,
            List<DomainTypeMirror> domainTypeMirrors,
            DiagramService diagramService) {
        this.sessionStorage = sessionStorage;
        this.diagram = diagram;
        this.project = project;
        this.diagramService = diagramService;
        this.domainTypeMirrors = domainTypeMirrors;

        setWidthFull();
        createDetails();
    }

    private void createDetails() {
        log.debug("createDetails DiagramVisibilityComponent started");
        Details packageDetails = new Details("Explicitly included packages");
        packageDetails.setWidthFull();
        packageDetails.setOpened(sessionStorage.isPackageFilterOpen());

        MultiSelectComboBox<String> packageMultiSelectComboBox =
            new PackageMultiSelectComboBox(domainTypeMirrors, diagram);
        packageMultiSelectComboBox.addValueChangeListener(e -> {
            diagram.setDomainModelVisibility(diagram.getDomainModelVisibility().replaceExplicitlyIncludedPackagesNames(e.getValue()));
            diagramService.updateAndRegenerate(diagram, project);
            ComponentUtil.fireEvent(
                UI.getCurrent(), new DiagramStylingChangedEvent(this, false));
        });

        packageDetails.add(packageMultiSelectComboBox);
        packageDetails.addOpenedChangeListener(e -> sessionStorage.setPackageFilterOpen(e.isOpened()));

        Details advancedFilterDetails = new Details("Advanced diagram trimming");
        advancedFilterDetails.setWidthFull();
        advancedFilterDetails.setOpened(sessionStorage.isAdvancedTrimmingOpen());
        advancedFilterDetails.addOpenedChangeListener(e -> sessionStorage.setAdvancedTrimmingOpen(e.isOpened()));

        List<DomainTypeMirror> items = filterConcreteMirrorsInterfaceAvailable();

        comboBoxConnected = createAndConfigureComboBox("Include Connections to:", items,
            diagram.getDomainModelVisibility().getIncludeConnectedToClassNames(), Collections.emptySet());
        advancedFilterDetails.add(comboBoxConnected);

        comboBoxConnectedIngoing = createAndConfigureComboBox("Include ingoing connections to:", items,
            diagram.getDomainModelVisibility().getIncludeConnectedToIngoingClassNames(),
            diagram.getDomainModelVisibility().getExcludeConnectedToIngoingClassNames());
        advancedFilterDetails.add(comboBoxConnectedIngoing);

        comboBoxConnectedOutgoing = createAndConfigureComboBox("Include outgoing connections from:", items,
            diagram.getDomainModelVisibility().getIncludeConnectedToOutgoingClassNames(),
            diagram.getDomainModelVisibility().getExcludeConnectedToOutgoingClassNames());
        advancedFilterDetails.add(comboBoxConnectedOutgoing);

        comboBoxConnectedExcludeIngoing = createAndConfigureComboBox("Exclude ingoing connections to:", items,
            diagram.getDomainModelVisibility().getExcludeConnectedToIngoingClassNames(),
            diagram.getDomainModelVisibility().getIncludeConnectedToIngoingClassNames());
        advancedFilterDetails.add(comboBoxConnectedExcludeIngoing);

        comboBoxConnectedExcludeOutgoing = createAndConfigureComboBox("Exclude outgoing connections from:", items,
            diagram.getDomainModelVisibility().getExcludeConnectedToOutgoingClassNames(),
            diagram.getDomainModelVisibility().getIncludeConnectedToOutgoingClassNames());
        advancedFilterDetails.add(comboBoxConnectedExcludeOutgoing);

        comboBoxInvisibleDomainObjects = createAndConfigureComboBox("Invisible domain objects:", items,
            diagram.getDomainModelVisibility().getBlacklistedClassNames(), Collections.emptySet());
        advancedFilterDetails.add(comboBoxInvisibleDomainObjects);

        add(packageDetails);
        add(advancedFilterDetails);
        log.debug("createDetails DiagramVisibilityComponent finished");
    }

    private MultiSelectComboBox<DomainTypeMirror> createAndConfigureComboBox(String label, List<DomainTypeMirror> items, Set<String> classNames, Set<String> complementaryClassNames) {
        HashSet<DomainTypeMirror> complementDomainTypeMirrors = new HashSet<>(items);
        complementDomainTypeMirrors.removeIf(
            typeMirror -> complementaryClassNames.contains(typeMirror.getTypeName()));

        MultiSelectComboBox<DomainTypeMirror> multiSelectComboBox = new MultiSelectComboBox<>(label);
        multiSelectComboBox.setWidthFull();
        multiSelectComboBox.setItems(complementDomainTypeMirrors);
        multiSelectComboBox.setItemLabelGenerator(this::name);
        multiSelectComboBox.select(selected(classNames));
        multiSelectComboBox.addValueChangeListener(e -> regenerateDiagram(
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

    private DomainTypeMirror[] selected(Set<String> typeNames){
        return filterConcreteMirrorsInterfaceAvailable().stream()
                .filter(dt -> typeNames.contains(dt.getTypeName()))
                .toArray(DomainTypeMirror[]::new);
    }

    private void regenerateDiagram(
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
        diagramService.updateAndRegenerate(diagram, project);
        ComponentUtil.fireEvent(UI.getCurrent(), new DiagramStylingChangedEvent(this, false));
    }

    private List<DomainTypeMirror> filterConcreteMirrorsInterfaceAvailable() {
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
}
