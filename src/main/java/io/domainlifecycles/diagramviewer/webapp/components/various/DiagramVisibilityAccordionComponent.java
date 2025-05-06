package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
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
import org.apache.commons.text.diff.StringsComparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class DiagramVisibilityAccordionComponent extends Accordion {

    private final DiagramService diagramService;
    private final Project project;
    private final Diagram diagram;

    public DiagramVisibilityAccordionComponent(Project project, Diagram diagram, DiagramService diagramService) {
        setWidthFull();
        this.diagramService = diagramService;
        this.project = project;
        this.diagram = diagram;
        createAccordion();
    }

    private void createAccordion() {
        Map<DomainType, ? extends List<? extends DomainTypeMirror>> groupedByDomainMirrorType = project.getDomainMirror()
            .getAllDomainTypeMirrors()
            .stream()
            .collect(groupingBy(DomainTypeMirror::getDomainType));

        for (DomainType type: domainTypeOrdered()) {
            List<? extends DomainTypeMirror> mirrors = filterConcreteMirrorsInterfaceAvailable(groupedByDomainMirrorType.get(type));

            if (!mirrors.isEmpty()) {
                AccordionPanel panel = new AccordionPanel(translateDomainType(type));
                VerticalLayout layout = new VerticalLayout();

                for (DomainTypeMirror mirror : mirrors) {
                    HorizontalLayout typeLayout = new HorizontalLayout();
                    typeLayout.setAlignItems(Alignment.CENTER);
                    NativeLabel typeNameLabel = new NativeLabel(shortClassName(mirror.getTypeName()));
                    HorizontalLayout buttonLayout = new HorizontalLayout();
                    buttonLayout.setWrap(false);

                    if(!List.of(DomainType.AGGREGATE_ROOT, DomainType.ENTITY, DomainType.VALUE_OBJECT, DomainType.ENUM, DomainType.IDENTITY).contains(type)) {
                        Button visibleButton = new Button(new Icon("vaadin:eye-slash"));
                        visibleButton.addThemeVariants(ButtonVariant.LUMO_SMALL);

                        if (diagram.getDomainModelVisibility().getBlacklistedClassNames().contains(mirror.getTypeName())) {
                            visibleButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                        }

                        visibleButton.addClickListener(e -> {
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
                        });

                        buttonLayout.add(visibleButton);
                        if (DomainType.APPLICATION_SERVICE.equals(type)) {
                            Button seedButton = new Button(new Icon("vaadin:filter"));
                            seedButton.addThemeVariants(ButtonVariant.LUMO_SMALL);

                            if (diagram.getDomainModelVisibility().getSeedClassNames().contains(mirror.getTypeName())) {
                                seedButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                            }

                            buttonLayout.add(seedButton);
                            seedButton.addClickListener(e -> {
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
                            });
                        }
                    }
                    typeLayout.add(buttonLayout);
                    typeLayout.add(typeNameLabel);
                    typeLayout.setWrap(false);
                    layout.add(typeLayout);
                }
                panel.add(layout);
                add(panel);
            }
        }
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
        list.add(DomainType.QUERY_HANDLER);
        list.add(DomainType.READ_MODEL);
        list.add(DomainType.OUTBOUND_SERVICE);
        list.add(DomainType.SERVICE_KIND);
        list.add(DomainType.NON_DOMAIN);

        return list;
    }

    private List<? extends DomainTypeMirror> filterConcreteMirrorsInterfaceAvailable(List<? extends DomainTypeMirror> mirrors) {
        List<DomainTypeMirror> list = new ArrayList<>();

        if(mirrors != null && mirrors.size() > 0) {
            List<String> mirroredTypeNames = mirrors.stream().filter(m -> !m.getTypeName().startsWith("io.domainlifecycles"))
                    .map(DomainTypeMirror::getTypeName).toList();
            list.addAll(
                    mirrors
                    .stream()
                    .filter(m -> !m.getTypeName().startsWith("io.domainlifecycles"))
                    .filter(m ->
                            !m.getDomainType().equals(DomainType.ENTITY) &&
                            !m.getDomainType().equals(DomainType.VALUE_OBJECT) &&
                            !m.getDomainType().equals(DomainType.ENUM) &&
                            !m.getDomainType().equals(DomainType.IDENTITY)
                    )
                    .toList()
            );

            for (DomainTypeMirror mirror : mirrors) {
                for(String interfaceTypeName : mirror.getAllInterfaceTypeNames()){
                    if(mirroredTypeNames.contains(interfaceTypeName)){
                        list.remove(mirror);
                    }
                }
            }
        }

        return list.stream().sorted(
                (a,b)-> {
                    return a.getTypeName().compareTo(b.getTypeName());
                }).collect(Collectors.toList());
    }
}
