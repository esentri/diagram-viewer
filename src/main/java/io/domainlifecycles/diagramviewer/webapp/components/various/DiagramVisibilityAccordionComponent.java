package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.model.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramStylingChangedEvent;
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

public class DiagramVisibilityAccordionComponent extends Accordion {

    private static final String DOMAINLIFECYCLES_PACKAGE_NAME = "io.domainlifecycles";

    private final DiagramService diagramService;
    private final Project project;
    private final Diagram diagram;

    public DiagramVisibilityAccordionComponent(Project project, Diagram diagram,
                                               List<DomainTypeMirror> domainTypeMirrors,
                                               DiagramService diagramService) {
        setWidthFull();
        this.diagramService = diagramService;
        this.project = project;
        this.diagram = diagram;
        createAccordion(domainTypeMirrors);
    }

    private void createAccordion(List<DomainTypeMirror> domainTypeMirrors) {
        List<DomainTypeMirror> directlyContained = domainTypeMirrors.stream().filter(dtm ->
            diagram.getDomainModelVisibility()
                .getFilteredPackageNames()
                .stream()
                .anyMatch(p -> dtm.getTypeName().startsWith(p))
        ).toList();

        Map<DomainType, ? extends List<? extends DomainTypeMirror>> typeMirrorsGroupedByDomainMirrorType =
            domainTypeMirrors
                .stream()
                .filter(dtm -> {
                    Set<String> filteredPackageNames = diagram.getDomainModelVisibility()
                        .getFilteredPackageNames();

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
                AccordionPanel panel = createAndGetAccordionPanelForDomainType(type, mirrors);
                add(panel);
            }
        }
    }

    private AccordionPanel createAndGetAccordionPanelForDomainType(DomainType type, List<? extends DomainTypeMirror> domainTypeMirrors) {
        AccordionPanel panel = new AccordionPanel(translateDomainType(type));
        VerticalLayout layout = new VerticalLayout();

        for (DomainTypeMirror mirror : domainTypeMirrors) {
            HorizontalLayout typeMirrorVisibilityLayout = createAndGetAccordionPanelContentForDomainTypeAndMirror(type, mirror);
            layout.add(typeMirrorVisibilityLayout);
        }

        panel.add(layout);
        return panel;
    }

    private HorizontalLayout createAndGetAccordionPanelContentForDomainTypeAndMirror(DomainType type, DomainTypeMirror mirror) {
        HorizontalLayout typeMirrorVisibilityLayout = new HorizontalLayout();
        typeMirrorVisibilityLayout.setAlignItems(Alignment.CENTER);
        typeMirrorVisibilityLayout.setWrap(false);

        NativeLabel typeMirrorNameLabel = new NativeLabel(shortClassName(mirror.getTypeName()));
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWrap(false);

        if (domainTypeOrdered().contains(type) && !DomainType.ENUM.equals(type)) {
            Button visibleButton = createAndGetDomainTypeVisibilityButton(mirror);
            buttonLayout.add(visibleButton);
            if(!DomainType.VALUE_OBJECT.equals(type) && !DomainType.ENTITY.equals(type)) {
                Button seedButton = createAndGetDomainTypeSeedButton(mirror);
                buttonLayout.add(seedButton);
            }
        }

        typeMirrorVisibilityLayout.add(buttonLayout);
        typeMirrorVisibilityLayout.add(typeMirrorNameLabel);
        return typeMirrorVisibilityLayout;
    }

    private Button createAndGetDomainTypeSeedButton(DomainTypeMirror mirror) {
        Button seedButton = new Button(new Icon(VaadinIcon.FILTER));
        seedButton.addThemeVariants(ButtonVariant.LUMO_SMALL);

        if (diagram.getDomainModelVisibility().getSeedClassNames().contains(mirror.getTypeName())) {
            seedButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        }

        seedButton.addClickListener(e -> seedMirrorChanged(mirror, seedButton));
        return seedButton;
    }

    private Button createAndGetDomainTypeVisibilityButton(DomainTypeMirror mirror) {
        Button visibleButton = new Button(new Icon(VaadinIcon.EYE_SLASH));
        visibleButton.addThemeVariants(ButtonVariant.LUMO_SMALL);

        if (diagram.getDomainModelVisibility().getBlacklistedClassNames().contains(
            mirror.getTypeName())) {
            visibleButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        }

        visibleButton.addClickListener(e -> mirrorVisibilityChanged(mirror, visibleButton));
        return visibleButton;
    }

    private void seedMirrorChanged(DomainTypeMirror mirror, Button seedButton) {
        String typeName = mirror.getTypeName();
        DomainModelVisibility visibility = diagram.getDomainModelVisibility();
        Set<String> seed = new HashSet<>(visibility.getSeedClassNames());
        boolean activated = seed.contains(typeName);

        if (activated) {
            seed.remove(mirror.getTypeName());
            seedButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
        } else {
            seed.add(mirror.getTypeName());
            seedButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        }

        diagram.setDomainModelVisibility(visibility.replaceSeedClassNames(seed));
        diagramService.update(diagram, project);
        ComponentUtil.fireEvent(UI.getCurrent(), new DiagramStylingChangedEvent(this, false));
    }

    private void mirrorVisibilityChanged(DomainTypeMirror mirror, Button visibleButton) {
        String typeName = mirror.getTypeName();
        boolean activated = diagram.getDomainModelVisibility().getBlacklistedClassNames().contains(typeName);
        DomainModelVisibility visibility = diagram.getDomainModelVisibility();
        Set<String> blackListedClassNames = new HashSet<>(visibility.getBlacklistedClassNames());

        if (activated) {
            blackListedClassNames.remove(mirror.getTypeName());
            visibleButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
        } else {
            blackListedClassNames.add(mirror.getTypeName());
            visibleButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        }

        diagram.setDomainModelVisibility(visibility.replaceBlacklistedClassNames(blackListedClassNames));
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
}
