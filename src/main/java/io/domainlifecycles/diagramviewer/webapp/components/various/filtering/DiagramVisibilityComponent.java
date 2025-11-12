package io.domainlifecycles.diagramviewer.webapp.components.various.filtering;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
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
import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
public class DiagramVisibilityComponent extends Div {

    private static final String DOMAINLIFECYCLES_PACKAGE_NAME = "io.domainlifecycles";

    private final DiagramService diagramService;
    private final SessionStorage sessionStorage;
    private Diagram currentDiagram;

    public DiagramVisibilityComponent(
            SessionStorage sessionStorage,
            DiagramService diagramService) {
        setWidthFull();
        this.sessionStorage = sessionStorage;
        this.diagramService = diagramService;
    }

    public void setDiagram(Diagram diagram) {
        this.currentDiagram = diagram;
        refreshDetails();
    }

    private void refreshDetails() {
        removeAll();
        if(currentDiagram != null) {
            var domainTypeMirrors = sessionStorage.getAllDomainTypeMirrorsWithoutEnumsAndIds(currentDiagram.getProject().getId());

            log.debug("refreshDetails DiagramVisibilityComponent started");
            List<DomainTypeMirror> directlyContained = domainTypeMirrors.stream().filter(dtm ->
                    currentDiagram.getDomainModelVisibility()
                            .getExplicitlyIncludedPackagesNames()
                            .stream()
                            .anyMatch(p -> dtm.getTypeName().startsWith(p))
            ).toList();
            log.debug("createDetails mirrors filtered");

            Map<DomainType, ? extends List<? extends DomainTypeMirror>> typeMirrorsGroupedByDomainMirrorType =
                    domainTypeMirrors
                            .stream()
                            .filter(dtm -> {
                                Set<String> filteredPackageNames = currentDiagram.getDomainModelVisibility()
                                        .getExplicitlyIncludedPackagesNames();

                                if (filteredPackageNames == null || filteredPackageNames.isEmpty()) return true;

                                return directlyContained.contains(dtm);
                            })
                            .collect(groupingBy(DomainTypeMirror::getDomainType));
            log.debug("createDetails mirrors grouped");
            for (DomainType type : domainTypeOrdered()) {
                List<? extends DomainTypeMirror> mirrors = filterConcreteMirrorsInterfaceAvailable(
                        currentDiagram,
                        typeMirrorsGroupedByDomainMirrorType.get(type)
                );

                if (!mirrors.isEmpty()) {
                    Details details = createAndGetDetailsLayoutForDomainType(type, mirrors);
                    add(details);
                }
            }
        }
        log.debug("createDetails DiagramVisibilityComponent finished");
    }

    private Details createAndGetDetailsLayoutForDomainType(DomainType type, List<? extends DomainTypeMirror> domainTypeMirrors) {
        log.debug("createAndGetDetailsLayoutForDomainType started {}", type);
        Details details = new Details(translateDomainType(type));
        if(this.sessionStorage.isDomainTypeSettingOpen(type)){
            details.setOpened(true);
            openDomainTypeView(
                details,
                true,
                type,
                domainTypeMirrors
            );
        }else{
            details.setOpened(false);
        }

        details.addOpenedChangeListener(event -> {
            this.sessionStorage.setDomainTypeSettingOpen(type, event.isOpened());
            openDomainTypeView(
                details,
                event.isOpened(),
                type,
                domainTypeMirrors
            );
        });
        log.debug("createAndGetDetailsLayoutForDomainType finished {}", type);
        return details;
    }

    private void openDomainTypeView(
            Details details,
            boolean opened,
            DomainType type,
            List<? extends DomainTypeMirror> domainTypeMirrors
    ){
        if(opened){
            VerticalLayout layout = new VerticalLayout();
            layout.setSpacing(false);
            layout.setPadding(false);
            for (DomainTypeMirror mirror : domainTypeMirrors) {
                Component typeMirrorVisibilityLayout = createAndGetContentForDomainTypeAndMirror(type, mirror);
                layout.add(typeMirrorVisibilityLayout);
            }
            details.add(layout);
        }else{
            details.removeAll();
        }

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
            Checkbox visible = createAndGetDomainTypeVisibilityCheckbox(mirror);
            blendingLayout.add(visible);
            if(!DomainType.VALUE_OBJECT.equals(type) && !DomainType.ENTITY.equals(type)) {
                RadioButtonGroup<VisibilityFilterType> radioGroup = new RadioButtonGroup<>();
                radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
                radioGroup.setLabel("Trimming");
                radioGroup.setItems(
                        VisibilityFilterType.NO_TRIMMING,
                        VisibilityFilterType.INCLUDE_CONNECTED,
                        VisibilityFilterType.INCLUDE_CONNECTED_INGOING,
                        VisibilityFilterType.INCLUDE_CONNECTED_OUTGOING,
                        VisibilityFilterType.EXCLUDE_CONNECTED_INGOING,
                        VisibilityFilterType.EXCLUDE_CONNECTED_OUTGOING
                );
                radioGroup.setItemLabelGenerator((ItemLabelGenerator<VisibilityFilterType>) item -> item.label);
                radioGroup.setValue(calculateRadioValue(mirror));
                radioGroup.addValueChangeListener(event -> {
                    mirrorVisibilityChanged(mirror, event.getValue());
                });
                blendingLayout.add(radioGroup);
            }

        }
        typeMirrorVisibilityLayout.add(typeMirrorNameLabel);
        typeMirrorVisibilityLayout.add(blendingLayout);
        return typeMirrorVisibilityLayout;
    }

    private VisibilityFilterType calculateRadioValue(DomainTypeMirror mirror) {
        if(currentDiagram.getDomainModelVisibility().getIncludeConnectedToClassNames().contains(mirror.getTypeName())){
            return VisibilityFilterType.INCLUDE_CONNECTED;
        }
        if(currentDiagram.getDomainModelVisibility().getIncludeConnectedToIngoingClassNames().contains(mirror.getTypeName())){
            return VisibilityFilterType.INCLUDE_CONNECTED_INGOING;
        }
        if(currentDiagram.getDomainModelVisibility().getIncludeConnectedToOutgoingClassNames().contains(mirror.getTypeName())){
            return VisibilityFilterType.INCLUDE_CONNECTED_OUTGOING;
        }
        if(currentDiagram.getDomainModelVisibility().getExcludeConnectedToIngoingClassNames().contains(mirror.getTypeName())){
            return VisibilityFilterType.EXCLUDE_CONNECTED_INGOING;
        }
        if(currentDiagram.getDomainModelVisibility().getExcludeConnectedToOutgoingClassNames().contains(mirror.getTypeName())){
            return VisibilityFilterType.EXCLUDE_CONNECTED_OUTGOING;
        }
        return VisibilityFilterType.NO_TRIMMING;
    }

    private Checkbox createAndGetDomainTypeVisibilityCheckbox(DomainTypeMirror mirror) {
        Checkbox visible = new Checkbox(VisibilityFilterType.VISIBLE.label);
        if (!currentDiagram.getDomainModelVisibility().getBlacklistedClassNames().contains(
                mirror.getTypeName())) {
            visible.setValue(true);
        }
        visible.addClickListener(e -> mirrorVisibilityChanged(mirror, VisibilityFilterType.VISIBLE));
        return visible;
    }

    private void mirrorVisibilityChanged(DomainTypeMirror mirror, VisibilityFilterType filterType) {
        String typeName = mirror.getTypeName();
        DomainModelVisibility visibility = currentDiagram.getDomainModelVisibility();

        switch (filterType) {
            case VISIBLE -> {
                boolean wasActivated = visibility.getBlacklistedClassNames().contains(typeName);
                var newBlacklistedClassNames = new HashSet<>(visibility.getBlacklistedClassNames());
                if(wasActivated) {
                    newBlacklistedClassNames.remove(typeName);
                }else{
                    newBlacklistedClassNames.add(typeName);
                }
                visibility = visibility.replaceBlacklistedClassNames(newBlacklistedClassNames);
            }
            case INCLUDE_CONNECTED -> {
                visibility = removeFromVisibilityAndAdd(typeName, visibility, VisibilityFilterType.INCLUDE_CONNECTED);
            }
            case INCLUDE_CONNECTED_INGOING -> {
                visibility = removeFromVisibilityAndAdd(typeName, visibility, VisibilityFilterType.INCLUDE_CONNECTED_INGOING);
            }
            case INCLUDE_CONNECTED_OUTGOING -> {
                visibility = removeFromVisibilityAndAdd(typeName, visibility, VisibilityFilterType.INCLUDE_CONNECTED_OUTGOING);
            }
            case EXCLUDE_CONNECTED_INGOING -> {
                visibility = removeFromVisibilityAndAdd(typeName, visibility, VisibilityFilterType.EXCLUDE_CONNECTED_INGOING);
            }
            case EXCLUDE_CONNECTED_OUTGOING -> {
                visibility = removeFromVisibilityAndAdd(typeName, visibility, VisibilityFilterType.EXCLUDE_CONNECTED_OUTGOING);
            }
            case NO_TRIMMING -> {
                visibility = removeFromVisibilityAndAdd(typeName, visibility, VisibilityFilterType.NO_TRIMMING);
            }
        }
        currentDiagram.setDomainModelVisibility(visibility);
        currentDiagram = diagramService.updateModelAndImage(currentDiagram);

        ComponentUtil.fireEvent(UI.getCurrent(), new DiagramStylingChangedEvent(this, false));

    }


    private DomainModelVisibility removeFromVisibilityAndAdd(String typeName, DomainModelVisibility visibility, VisibilityFilterType addType) {
        var newConnected = new HashSet<>(visibility.getIncludeConnectedToClassNames());
        newConnected.remove(typeName);

        var newIncludeIngoing = new HashSet<>(visibility.getIncludeConnectedToIngoingClassNames());
        newIncludeIngoing.remove(typeName);

        var newIncludeOutgoing = new HashSet<>(visibility.getIncludeConnectedToOutgoingClassNames());
        newIncludeOutgoing.remove(typeName);

        var newExcludeIngoing = new HashSet<>(visibility.getExcludeConnectedToIngoingClassNames());
        newExcludeIngoing.remove(typeName);

        var newExcludeOutgoing = new HashSet<>(visibility.getExcludeConnectedToOutgoingClassNames());
        newExcludeOutgoing.remove(typeName);

        switch (addType) {
            case INCLUDE_CONNECTED -> newConnected.add(typeName);
            case INCLUDE_CONNECTED_INGOING -> newIncludeIngoing.add(typeName);
            case INCLUDE_CONNECTED_OUTGOING -> newIncludeOutgoing.add(typeName);
            case EXCLUDE_CONNECTED_INGOING -> newExcludeIngoing.add(typeName);
            case EXCLUDE_CONNECTED_OUTGOING -> newExcludeOutgoing.add(typeName);
        }

        visibility = visibility.replaceIncludeConnectedToClassNames(newConnected);
        visibility = visibility.replaceIncludeConnectedToIngoingClassNames(newIncludeIngoing);
        visibility = visibility.replaceIncludeConnectedToOutgoingClassNames(newIncludeOutgoing);
        visibility = visibility.replaceExcludeConnectedToIngoingClassNames(newExcludeIngoing);
        visibility = visibility.replaceExcludeConnectedToOutgoingClassNames(newExcludeOutgoing);
        return visibility;
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

    private List<? extends DomainTypeMirror> filterConcreteMirrorsInterfaceAvailable(
            Diagram diagram,
            List<? extends DomainTypeMirror> mirrors) {
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
        EXCLUDE_CONNECTED_OUTGOING("exclude outgoing connections"),
        NO_TRIMMING("no trimming");

        final String label;

        VisibilityFilterType(String label) {
            this.label = label;
        }
    }

}
