package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeLabel;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.groupingBy;

public class DiagramVisibilityComponent extends Div {

    private static final String DOMAINLIFECYCLES_PACKAGE_NAME = "io.domainlifecycles";

    private final DiagramService diagramService;
    private final Project project;
    private final Diagram diagram;
    private final SessionStorage sessionStorage;

    public DiagramVisibilityComponent(SessionStorage sessionStorage,
                                      Project project,
                                      Diagram diagram,
                                      List<DomainTypeMirror> domainTypeMirrors,
                                      DiagramService diagramService) {
        setWidthFull();
        this.sessionStorage = sessionStorage;
        this.diagramService = diagramService;
        this.project = project;
        this.diagram = diagram;
        createDetails(domainTypeMirrors);
    }

    private void createDetails(List<DomainTypeMirror> domainTypeMirrors) {
        List<DomainTypeMirror> directlyContained = domainTypeMirrors.stream().filter(dtm ->
            diagram.getDomainModelVisibility()
                .getExplicitlyIncludedPackagesNames()
                .stream()
                .anyMatch(p -> dtm.getTypeName().startsWith(p))
        ).toList();

        Map<DomainType, ? extends List<? extends DomainTypeMirror>> typeMirrorsGroupedByDomainMirrorType =
            domainTypeMirrors
                .stream()
                .filter(dtm -> {
                    Set<String> filteredPackageNames = diagram.getDomainModelVisibility()
                        .getExplicitlyIncludedPackagesNames();

                    if(filteredPackageNames == null || filteredPackageNames.isEmpty()) return true;

                    return directlyContained.contains(dtm)
                        || directlyContained
                        .stream()
                        .flatMap(d -> d.getAllFields().stream())
                        .anyMatch(f -> f.getType().getTypeName().equals(dtm.getTypeName()));
                })
                .collect(groupingBy(DomainTypeMirror::getDomainType));

        for (DomainType type : domainTypeOrdered()) {
            List<? extends DomainTypeMirror> mirrors = filterConcreteMirrorsInterfaceAvailable(
                typeMirrorsGroupedByDomainMirrorType.get(type));

            if (!mirrors.isEmpty()) {
                Details details = createAndGetDetailsLayoutForDomainType(type, mirrors);
                add(details);
            }
        }
    }

    private Details createAndGetDetailsLayoutForDomainType(DomainType type, List<? extends DomainTypeMirror> domainTypeMirrors) {
        Details details = new Details(translateDomainType(type));
        details.setOpened(this.sessionStorage.isDomainTypeSettingOpen(type));
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);

        for (DomainTypeMirror mirror : domainTypeMirrors) {
            Component typeMirrorVisibilityLayout = createAndGetContentForDomainTypeAndMirror(type, mirror);
            layout.add(typeMirrorVisibilityLayout);
        }

        details.add(layout);
        details.addOpenedChangeListener(event -> this.sessionStorage.setDomainTypeSettingOpen(type, event.isOpened()));
        return details;
    }

    private Component createAndGetContentForDomainTypeAndMirror(DomainType type, DomainTypeMirror mirror) {
        VerticalLayout typeMirrorVisibilityLayout = new VerticalLayout();
        typeMirrorVisibilityLayout.setMargin(false);
        typeMirrorVisibilityLayout.setSpacing(false);
        typeMirrorVisibilityLayout.getStyle().setPaddingBottom("0");
        typeMirrorVisibilityLayout.getStyle().setPaddingTop("0");

        NativeLabel typeMirrorNameLabel = new NativeLabel(shortClassName(mirror.getTypeName()));
        typeMirrorNameLabel.getStyle().set("font-weight", "bold");

        Details blendingLayout = new Details("Trim settings");
        blendingLayout.addClassName("diagram-styling-details");
        blendingLayout.setOpened(false);

        if (domainTypeOrdered().contains(type) && !DomainType.ENUM.equals(type)) {
            Checkbox visible = createAndGetDomainTypeVisibilityCheckbox(mirror, VisibilityFilterType.VISIBLE);
            blendingLayout.add(visible);
            if(!DomainType.VALUE_OBJECT.equals(type) && !DomainType.ENTITY.equals(type)) {
                Checkbox includeConnections = createAndGetDomainTypeVisibilityCheckbox(mirror, VisibilityFilterType.INCLUDE_CONNECTED);
                blendingLayout.add(includeConnections);
                Checkbox includeConnectionsIn = createAndGetDomainTypeVisibilityCheckbox(mirror, VisibilityFilterType.INCLUDE_CONNECTED_INGOING);
                blendingLayout.add(includeConnectionsIn);
                Checkbox includeConnectionsOut = createAndGetDomainTypeVisibilityCheckbox(mirror, VisibilityFilterType.INCLUDE_CONNECTED_OUTGOING);
                blendingLayout.add(includeConnectionsOut);
                Checkbox excludeConnectionsIn = createAndGetDomainTypeVisibilityCheckbox(mirror, VisibilityFilterType.EXCLUDE_CONNECTED_INGOING);
                blendingLayout.add(excludeConnectionsIn);
                Checkbox excludeConnectionsOut = createAndGetDomainTypeVisibilityCheckbox(mirror, VisibilityFilterType.EXCLUDE_CONNECTED_OUTGOING);
                blendingLayout.add(excludeConnectionsOut);
            }
        }
        typeMirrorVisibilityLayout.add(typeMirrorNameLabel);
        typeMirrorVisibilityLayout.add(blendingLayout);
        return typeMirrorVisibilityLayout;
    }



    private Checkbox createAndGetDomainTypeVisibilityCheckbox(DomainTypeMirror mirror, VisibilityFilterType filterType) {
        Checkbox visible = new Checkbox(filterType.label);

        switch (filterType) {
            case VISIBLE -> {
                if (diagram.getDomainModelVisibility().getBlacklistedClassNames().contains(
                        mirror.getTypeName())) {
                    visible.setValue(true);
                }
            }
            case INCLUDE_CONNECTED -> {
                if (diagram.getDomainModelVisibility().getIncludeConnectedToClassNames().contains(
                        mirror.getTypeName())) {
                    visible.setValue(true);
                }
            }
            case INCLUDE_CONNECTED_INGOING -> {
                if (diagram.getDomainModelVisibility().getIncludeConnectedToIngoingClassNames().contains(
                        mirror.getTypeName())) {
                    visible.setValue(true);
                }
            }
            case INCLUDE_CONNECTED_OUTGOING -> {
                if (diagram.getDomainModelVisibility().getIncludeConnectedToOutgoingClassNames().contains(
                        mirror.getTypeName())) {
                    visible.setValue(true);
                }
            }
            case EXCLUDE_CONNECTED_INGOING -> {
                if (diagram.getDomainModelVisibility().getExcludeConnectedToIngoingClassNames().contains(
                        mirror.getTypeName())) {
                    visible.setValue(true);
                }
            }
            case EXCLUDE_CONNECTED_OUTGOING -> {
                if (diagram.getDomainModelVisibility().getExcludeConnectedToOutgoingClassNames().contains(
                        mirror.getTypeName())) {
                    visible.setValue(true);
                }
            }
        }

        visible.addClickListener(e -> mirrorVisibilityChanged(mirror, filterType));
        return visible;
    }

    private void mirrorVisibilityChanged(DomainTypeMirror mirror, VisibilityFilterType filterType) {
        String typeName = mirror.getTypeName();
        DomainModelVisibility visibility = diagram.getDomainModelVisibility();

        switch (filterType) {
            case VISIBLE -> {
                boolean wasActivated = diagram.getDomainModelVisibility().getBlacklistedClassNames().contains(typeName);
                Set<String> classNames = new HashSet<>(visibility.getBlacklistedClassNames());
                if(wasActivated) {
                    classNames.remove(typeName);
                }else{
                    classNames.add(typeName);
                }
                diagram.setDomainModelVisibility(visibility.replaceBlacklistedClassNames(classNames));
            }
            case INCLUDE_CONNECTED -> {
                boolean wasActivated = diagram.getDomainModelVisibility().getIncludeConnectedToClassNames().contains(typeName);
                Set<String> classNames = new HashSet<>(visibility.getIncludeConnectedToClassNames());
                if(wasActivated) {
                    classNames.remove(typeName);
                }else{
                    classNames.add(typeName);
                }
                diagram.setDomainModelVisibility(visibility.replaceIncludeConnectedToClassNames(classNames));
            }
            case INCLUDE_CONNECTED_INGOING -> {
                boolean wasActivated = diagram.getDomainModelVisibility().getIncludeConnectedToIngoingClassNames().contains(typeName);
                Set<String> classNames = new HashSet<>(visibility.getIncludeConnectedToIngoingClassNames());
                if(wasActivated) {
                    classNames.remove(typeName);
                }else{
                    classNames.add(typeName);
                }
                diagram.setDomainModelVisibility(visibility.replaceIncludeConnectedToIngoingClassNames(classNames));
            }
            case INCLUDE_CONNECTED_OUTGOING -> {
                boolean wasActivated = diagram.getDomainModelVisibility().getIncludeConnectedToOutgoingClassNames().contains(typeName);
                Set<String> classNames = new HashSet<>(visibility.getIncludeConnectedToOutgoingClassNames());
                if(wasActivated) {
                    classNames.remove(typeName);
                }else{
                    classNames.add(typeName);
                }
                diagram.setDomainModelVisibility(visibility.replaceIncludeConnectedToOutgoingClassNames(classNames));
            }
            case EXCLUDE_CONNECTED_INGOING -> {
                boolean wasActivated = diagram.getDomainModelVisibility().getExcludeConnectedToIngoingClassNames().contains(typeName);
                Set<String> classNames = new HashSet<>(visibility.getExcludeConnectedToIngoingClassNames());
                if(wasActivated) {
                    classNames.remove(typeName);
                }else{
                    classNames.add(typeName);
                }
                diagram.setDomainModelVisibility(visibility.replaceExcludeConnectedToIngoingClassNames(classNames));
            }
            case EXCLUDE_CONNECTED_OUTGOING -> {
                boolean wasActivated = diagram.getDomainModelVisibility().getExcludeConnectedToOutgoingClassNames().contains(typeName);
                Set<String> classNames = new HashSet<>(visibility.getExcludeConnectedToOutgoingClassNames());
                if(wasActivated) {
                    classNames.remove(typeName);
                }else{
                    classNames.add(typeName);
                }
                diagram.setDomainModelVisibility(visibility.replaceExcludeConnectedToOutgoingClassNames(classNames));
            }
        }

        diagramService.update(diagram, project);
        ComponentUtil.fireEvent(
            UI.getCurrent(), new DiagramStylingChangedEvent(this, false));
    }

    private String shortClassName(String fullClassName) {
        return fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
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

    private List<DomainType> domainTypeOrdered() {
        List<DomainType> list = new ArrayList<>();

        list.add(DomainType.APPLICATION_SERVICE);
        list.add(DomainType.DOMAIN_SERVICE);
        list.add(DomainType.DOMAIN_COMMAND);
        list.add(DomainType.DOMAIN_EVENT);
        list.add(DomainType.REPOSITORY);
        list.add(DomainType.AGGREGATE_ROOT);
        list.add(DomainType.ENTITY);
        list.add(DomainType.VALUE_OBJECT);
        list.add(DomainType.QUERY_HANDLER);
        list.add(DomainType.READ_MODEL);
        list.add(DomainType.OUTBOUND_SERVICE);
        list.add(DomainType.SERVICE_KIND);
        list.add(DomainType.NON_DOMAIN);

        return list;
    }

    private List<? extends DomainTypeMirror> filterConcreteMirrorsInterfaceAvailable(List<? extends DomainTypeMirror> mirrors) {
        List<DomainTypeMirror> domainTypeMirrors = new ArrayList<>();

        if (mirrors != null && mirrors.size() > 0) {
            List<String> mirroredTypeNames = mirrors.stream().map(DomainTypeMirror::getTypeName)
                .filter(typeName ->
                        diagram.getDomainModelVisibility().getExplicitlyIncludedPackagesNames().isEmpty()
                                || diagram.getDomainModelVisibility().getExplicitlyIncludedPackagesNames().stream()
                                .anyMatch(typeName::startsWith))
                .filter(typeName -> !typeName.startsWith(DOMAINLIFECYCLES_PACKAGE_NAME)).toList();
            domainTypeMirrors.addAll(
                mirrors
                    .stream()
                    .filter(m -> !m.getTypeName().startsWith(DOMAINLIFECYCLES_PACKAGE_NAME))
                    .filter(m ->
                        !m.getDomainType().equals(DomainType.ENUM) &&
                        !m.getDomainType().equals(DomainType.IDENTITY)
                    )
                    .toList()
            );

            for (DomainTypeMirror mirror : mirrors) {
                for (String interfaceTypeName : mirror.getAllInterfaceTypeNames()) {
                    if (mirroredTypeNames.contains(interfaceTypeName)) {
                        domainTypeMirrors.remove(mirror);
                    }
                }
            }
        }

        return domainTypeMirrors.stream().sorted(
            Comparator.comparing(DomainTypeMirror::getTypeName)).collect(Collectors.toList());
    }

    private enum VisibilityFilterType {
        VISIBLE("visible"),
        INCLUDE_CONNECTED("include connections"),
        INCLUDE_CONNECTED_INGOING ("include ingoing connections"),
        INCLUDE_CONNECTED_OUTGOING("include outgoing connections"),
        EXCLUDE_CONNECTED_INGOING("exclude ingoing connections"),
        EXCLUDE_CONNECTED_OUTGOING("exclude outgoing connections");

        final String label;

        VisibilityFilterType(String label) {
            this.label = label;
        }
    }
}
