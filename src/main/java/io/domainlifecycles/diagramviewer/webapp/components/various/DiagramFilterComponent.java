package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramStylingChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.mirror.api.DomainType;
import io.domainlifecycles.mirror.api.DomainTypeMirror;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        Details packageDetails = new Details("Explicitly included packages");
        packageDetails.setWidthFull();
        packageDetails.setOpened(sessionStorage.isPackageFilterOpen());

        MultiSelectComboBox<String> packageMultiSelectComboBox =
            new PackageMultiSelectComboBox(domainTypeMirrors, diagram, project, diagramService);

        packageDetails.add(packageMultiSelectComboBox);
        packageDetails.addOpenedChangeListener(e -> sessionStorage.setPackageFilterOpen(e.isOpened()));

        Details advancedFilterDetails = new Details("Advanced diagram trimming");
        advancedFilterDetails.setWidthFull();
        advancedFilterDetails.setOpened(sessionStorage.isAdvancedTrimmingOpen());
        advancedFilterDetails.addOpenedChangeListener(e -> sessionStorage.setAdvancedTrimmingOpen(e.isOpened()));

        List<DomainTypeMirror> items = filterConcreteMirrorsInterfaceAvailable();

        comboBoxConnected = createAndConfigureCheckbox("Include Connections to:", items,
            diagram.getDomainModelVisibility().getIncludeConnectedToClassNames());
        advancedFilterDetails.add(comboBoxConnected);

        comboBoxConnectedIngoing = createAndConfigureCheckbox("Include ingoing connections to:", items,
            diagram.getDomainModelVisibility().getIncludeConnectedToIngoingClassNames());
        advancedFilterDetails.add(comboBoxConnectedIngoing);

        comboBoxConnectedOutgoing = createAndConfigureCheckbox("Include outgoing connections from:", items,
            diagram.getDomainModelVisibility().getIncludeConnectedToOutgoingClassNames());
        advancedFilterDetails.add(comboBoxConnectedOutgoing);

        comboBoxConnectedExcludeIngoing = createAndConfigureCheckbox("Exclude ingoing connections to:", items,
            diagram.getDomainModelVisibility().getExcludeConnectedToIngoingClassNames());
        advancedFilterDetails.add(comboBoxConnectedExcludeIngoing);

        comboBoxConnectedExcludeOutgoing = createAndConfigureCheckbox("Exclude outgoing connections from:", items,
            diagram.getDomainModelVisibility().getExcludeConnectedToOutgoingClassNames());
        advancedFilterDetails.add(comboBoxConnectedExcludeOutgoing);

        comboBoxInvisibleDomainObjects = createAndConfigureCheckbox("Invisible domain objects:", items,
            diagram.getDomainModelVisibility().getBlacklistedClassNames());
        advancedFilterDetails.add(comboBoxInvisibleDomainObjects);

        add(packageDetails);
        add(advancedFilterDetails);
    }

    private MultiSelectComboBox<DomainTypeMirror> createAndConfigureCheckbox(String label, List<DomainTypeMirror> items, Set<String> classNames) {
        MultiSelectComboBox<DomainTypeMirror> comboBox = new MultiSelectComboBox<>(label);
        comboBox.setWidthFull();
        comboBox.setItems(items);
        comboBox.setItemLabelGenerator(this::name);
        comboBox.select(selected(classNames));
        comboBox.addValueChangeListener(e -> regenerateDiagram(
            comboBoxConnected.getSelectedItems(),
            comboBoxConnectedIngoing.getSelectedItems(),
            comboBoxConnectedOutgoing.getSelectedItems(),
            comboBoxConnectedExcludeIngoing.getSelectedItems(),
            comboBoxConnectedExcludeOutgoing.getSelectedItems(),
            comboBoxInvisibleDomainObjects.getSelectedItems()
        ));

        return comboBox;
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
        diagramService.update(diagram, project);
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

            for (DomainTypeMirror mirror : domainTypeMirrors) {
                for (String interfaceTypeName : mirror.getAllInterfaceTypeNames()) {
                    if (mirroredTypeNames.contains(interfaceTypeName)) {
                        domainTypeMirrorsFiltered.remove(mirror);
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
