package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.ButtonVariant;
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

    private static final String DOMAINLIFECYCLES_PACKAGE_NAME = "io.domainlifecycles";

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
        createDetails();
    }

    private void createDetails() {
        Details packageDetails = new Details("Explicitly included packages");
        packageDetails.setOpened(sessionStorage.isPackageFilterOpen());

        PackageSelectChipField packageSelectChipField =
            new PackageSelectChipField(diagram.getDomainModelVisibility().getExplicitlyIncludedPackagesNames());
        packageSelectChipField.setWidthFull();
        packageSelectChipField.addValueChangeListener(e -> {
            diagram.setDomainModelVisibility(diagram.getDomainModelVisibility().replaceExplicitlyIncludedPackagesNames(e.getValue()));
            diagramService.update(diagram, project);
            ComponentUtil.fireEvent(
                UI.getCurrent(), new DiagramStylingChangedEvent(this, false));
        });

        packageDetails.add(packageSelectChipField);
        packageDetails.addOpenedChangeListener(e -> {
            this.sessionStorage.setPackageFilterOpen(e.isOpened());
        });

        Details advancedFilterDetails = new Details("Advanced diagram trimming");
        advancedFilterDetails.setOpened(sessionStorage.isAdvancedTrimmingOpen());
        advancedFilterDetails.addOpenedChangeListener(e -> {
            this.sessionStorage.setAdvancedTrimmingOpen(e.isOpened());
        });

        var items = filterConcreteMirrorsInterfaceAvailable();

        comboBoxConnected = new MultiSelectComboBox<>("Include Connections to:");
        comboBoxConnected.setItems(items);
        comboBoxConnected.setItemLabelGenerator(this::name);
        comboBoxConnected.select(selected(diagram.getDomainModelVisibility().getIncludeConnectedToClassNames()));
        comboBoxConnected.addValueChangeListener(e -> {
            regenerateDiagram(
                    comboBoxConnected.getSelectedItems(),
                    comboBoxConnectedIngoing.getSelectedItems(),
                    comboBoxConnectedOutgoing.getSelectedItems(),
                    comboBoxConnectedExcludeIngoing.getSelectedItems(),
                    comboBoxConnectedExcludeOutgoing.getSelectedItems(),
                    comboBoxInvisibleDomainObjects.getSelectedItems()
            );
        });
        advancedFilterDetails.add(comboBoxConnected);

        comboBoxConnectedIngoing = new MultiSelectComboBox<>("Include ingoing connections to:");
        comboBoxConnectedIngoing.setItems(items);
        comboBoxConnectedIngoing.setItemLabelGenerator(this::name);
        comboBoxConnectedIngoing.select(selected(diagram.getDomainModelVisibility().getIncludeConnectedToIngoingClassNames()));
        comboBoxConnectedIngoing.addValueChangeListener(e -> {
            regenerateDiagram(
                    comboBoxConnected.getSelectedItems(),
                    comboBoxConnectedIngoing.getSelectedItems(),
                    comboBoxConnectedOutgoing.getSelectedItems(),
                    comboBoxConnectedExcludeIngoing.getSelectedItems(),
                    comboBoxConnectedExcludeOutgoing.getSelectedItems(),
                    comboBoxInvisibleDomainObjects.getSelectedItems()
            );
        });
        advancedFilterDetails.add(comboBoxConnectedIngoing);

        comboBoxConnectedOutgoing = new MultiSelectComboBox<>("Include outgoing connections from:");
        comboBoxConnectedOutgoing.setItems(items);
        comboBoxConnectedOutgoing.setItemLabelGenerator(this::name);
        comboBoxConnectedOutgoing.select(selected(diagram.getDomainModelVisibility().getIncludeConnectedToOutgoingClassNames()));
        comboBoxConnectedOutgoing.addValueChangeListener(e -> {
            regenerateDiagram(
                    comboBoxConnected.getSelectedItems(),
                    comboBoxConnectedIngoing.getSelectedItems(),
                    comboBoxConnectedOutgoing.getSelectedItems(),
                    comboBoxConnectedExcludeIngoing.getSelectedItems(),
                    comboBoxConnectedExcludeOutgoing.getSelectedItems(),
                    comboBoxInvisibleDomainObjects.getSelectedItems()
            );
        });
        advancedFilterDetails.add(comboBoxConnectedOutgoing);

        comboBoxConnectedExcludeIngoing = new MultiSelectComboBox<>("Exclude ingoing connections to:");
        comboBoxConnectedExcludeIngoing.setItems(items);
        comboBoxConnectedExcludeIngoing.setItemLabelGenerator(this::name);
        comboBoxConnectedExcludeIngoing.select(selected(diagram.getDomainModelVisibility().getExcludeConnectedToIngoingClassNames()));
        comboBoxConnectedExcludeIngoing.addValueChangeListener(e -> {
            regenerateDiagram(
                    comboBoxConnected.getSelectedItems(),
                    comboBoxConnectedIngoing.getSelectedItems(),
                    comboBoxConnectedOutgoing.getSelectedItems(),
                    comboBoxConnectedExcludeIngoing.getSelectedItems(),
                    comboBoxConnectedExcludeOutgoing.getSelectedItems(),
                    comboBoxInvisibleDomainObjects.getSelectedItems()
            );
        });
        advancedFilterDetails.add(comboBoxConnectedExcludeIngoing);

        comboBoxConnectedExcludeOutgoing = new MultiSelectComboBox<>("Exclude outgoing connections from:");
        comboBoxConnectedExcludeOutgoing.setItems(items);
        comboBoxConnectedExcludeOutgoing.setItemLabelGenerator(this::name);
        comboBoxConnectedExcludeOutgoing.select(selected(diagram.getDomainModelVisibility().getExcludeConnectedToOutgoingClassNames()));
        comboBoxConnectedExcludeOutgoing.addValueChangeListener(e -> {
            regenerateDiagram(
                    comboBoxConnected.getSelectedItems(),
                    comboBoxConnectedIngoing.getSelectedItems(),
                    comboBoxConnectedOutgoing.getSelectedItems(),
                    comboBoxConnectedExcludeIngoing.getSelectedItems(),
                    comboBoxConnectedExcludeOutgoing.getSelectedItems(),
                    comboBoxInvisibleDomainObjects.getSelectedItems()
            );
        });
        advancedFilterDetails.add(comboBoxConnectedExcludeOutgoing);

        comboBoxInvisibleDomainObjects = new MultiSelectComboBox<>("Invisible domain objects:");
        comboBoxInvisibleDomainObjects.setItems(items);
        comboBoxInvisibleDomainObjects.setItemLabelGenerator(this::name);
        comboBoxInvisibleDomainObjects.select(selected(diagram.getDomainModelVisibility().getBlacklistedClassNames()));
        comboBoxInvisibleDomainObjects.addValueChangeListener(e -> {
            regenerateDiagram(
                    comboBoxConnected.getSelectedItems(),
                    comboBoxConnectedIngoing.getSelectedItems(),
                    comboBoxConnectedOutgoing.getSelectedItems(),
                    comboBoxConnectedExcludeIngoing.getSelectedItems(),
                    comboBoxConnectedExcludeOutgoing.getSelectedItems(),
                    comboBoxInvisibleDomainObjects.getSelectedItems()
            );
        });
        advancedFilterDetails.add(comboBoxInvisibleDomainObjects);

        add(packageDetails);
        add(advancedFilterDetails);
    }

    private String name(DomainTypeMirror mirror) {
        return mirror.getTypeName().substring(mirror.getTypeName().lastIndexOf('.') + 1)
                + " <" + translateDomainType(mirror.getDomainType())+">";
    }

    private DomainTypeMirror[] selected(Set<String> typeNames){
        var selected = filterConcreteMirrorsInterfaceAvailable().stream()
                .filter(dt -> typeNames.contains(dt.getTypeName()))
                .toArray(DomainTypeMirror[]::new);
        return selected;
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
                    .filter(m -> !m.getTypeName().startsWith(DOMAINLIFECYCLES_PACKAGE_NAME))
                    .map(DomainTypeMirror::getTypeName).toList();
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
