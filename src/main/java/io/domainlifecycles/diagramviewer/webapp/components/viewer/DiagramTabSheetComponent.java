package io.domainlifecycles.diagramviewer.webapp.components.viewer;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import io.domainlifecycles.diagramviewer.session.AnalyzedDomainModel;
import io.domainlifecycles.mirror.api.DomainType;
import io.domainlifecycles.mirror.api.DomainTypeMirror;

import java.util.ArrayList;
import java.util.List;


import static java.util.stream.Collectors.groupingBy;

public class DiagramTabSheetComponent extends FlexLayout {

    private final AnalyzedDomainModel analyzedDomainModel;
    private IFrame frame = null;
    private String diagramFileName;

    public DiagramTabSheetComponent(AnalyzedDomainModel analyzedDomainModel) {
        this.analyzedDomainModel = analyzedDomainModel;
        setSizeFull();
    }

    public void addTabs(String diagramFileName) {
        this.diagramFileName = diagramFileName;
        createZoomComponent(diagramFileName);
    }

    private void createZoomComponent(String diagramName){
        var zoomistContainer = generateZoomComponentContainer(diagramName);
        add(zoomistContainer);
    }

    private SplitLayout generateZoomComponentContainer(String diagramName) {
        var zoomFrame = newZoomFrame(diagramName);
        var accordion = createAccordion();
        var horizontalLayoutLeft = new HorizontalLayout();
        var horizontalLayoutRight = new HorizontalLayout();
        horizontalLayoutRight.setHeightFull();
        horizontalLayoutRight.add(accordion);
        horizontalLayoutLeft.add(zoomFrame);
        horizontalLayoutLeft.setFlexGrow(1, zoomFrame);
        SplitLayout zoomistContainer = new SplitLayout(horizontalLayoutLeft, horizontalLayoutRight);
        zoomistContainer.setHeightFull();
        zoomistContainer.setClassName("zoomist-container");
        zoomistContainer.setSplitterPosition(80);

        return zoomistContainer;
    }

    private IFrame newZoomFrame(String diagramName) {
        var frame = new IFrame("/simple/"+diagramName);
        frame.getStyle().clear();
        this.frame = frame;
        return frame;
    }

    private Accordion createAccordion() {
        Accordion accordion = new Accordion();
        var groupedByDomainMirrorType = analyzedDomainModel.getDomainModel()
                .allTypeMirrors()
                .values()
                .stream()
                .collect(groupingBy(tm -> tm.getDomainType()));
        for (DomainType type: domainTypeOrdered()) {
            var mirrors = filterConcreteMirrorsInterfaceAvailable(groupedByDomainMirrorType.get(type));
            if (mirrors != null && !mirrors.isEmpty()) {
                AccordionPanel panel = new AccordionPanel(translateDomainType(type));
                var layout = new VerticalLayout();
                for (var mirror : mirrors) {
                    var typeLayout = new HorizontalLayout();
                    var name = new NativeLabel(shortClassName(mirror.getTypeName()));
                    var buttonLayout = new HorizontalLayout();
                    buttonLayout.setWrap(false);
                    //var editButton = new Button(new Icon("vaadin:edit"));
                    //var deleteButton = new Button(new Icon("vaadin:close"));
                    //buttonLayout.add(editButton);
                    //buttonLayout.add(deleteButton);
                    if(!List.of(DomainType.AGGREGATE_ROOT, DomainType.ENTITY, DomainType.VALUE_OBJECT, DomainType.ENUM, DomainType.IDENTITY).contains(type)) {
                        var visibleButton = new Button(new Icon("vaadin:eye-slash"));
                        visibleButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
                        if (analyzedDomainModel.getDomainModelVisibility().blacklistedClassNames().contains(mirror.getTypeName())) {
                            visibleButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                        }
                        visibleButton.addClickListener(e -> {
                            var typeName = mirror.getTypeName();
                            var activated = analyzedDomainModel.getDomainModelVisibility().blacklistedClassNames().contains(typeName);
                            var visibility = analyzedDomainModel.getDomainModelVisibility();
                            var blackListed = new ArrayList<>(visibility.blacklistedClassNames());
                            if (activated) {
                                blackListed.remove(mirror.getTypeName());
                                visibleButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
                            } else {
                                blackListed.add(mirror.getTypeName());
                                visibleButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                            }
                            analyzedDomainModel.setDomainModelVisibility(visibility.replaceBlacklistedClassNames(blackListed));
                            refreshDiagram();
                        });
                        buttonLayout.add(visibleButton);
                        if (DomainType.APPLICATION_SERVICE.equals(type)) {
                            var seedButton = new Button(new Icon("vaadin:filter"));
                            seedButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
                            if (analyzedDomainModel.getDomainModelVisibility().seedClassNames().contains(mirror.getTypeName())) {
                                seedButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                            }
                            buttonLayout.add(seedButton);
                            seedButton.addClickListener(e -> {
                                var typeName = mirror.getTypeName();
                                var visibility = analyzedDomainModel.getDomainModelVisibility();
                                var seed = new ArrayList<>(visibility.seedClassNames());
                                var activated = seed.contains(typeName);
                                if (activated) {
                                    seed.remove(mirror.getTypeName());
                                    seedButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
                                } else {
                                    seed.add(mirror.getTypeName());
                                    seedButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                                }
                                analyzedDomainModel.setDomainModelVisibility(visibility.replaceSeedClassNames(seed));
                                refreshDiagram();
                            });
                        }
                    }
                    typeLayout.add(buttonLayout);
                    typeLayout.add(name);
                    typeLayout.setWrap(false);
                    layout.add(typeLayout);
                }
                panel.add(layout);
                accordion.add(panel);
            }
        }
        return accordion;
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

    private void refreshDiagram(){
        frame.reload();
    }

    private List<DomainType> domainTypeOrdered(){
        var list = new ArrayList<DomainType>();
        list.add(DomainType.APPLICATION_SERVICE);
        list.add(DomainType.DOMAIN_SERVICE);
        list.add(DomainType.DOMAIN_COMMAND);
        list.add(DomainType.DOMAIN_EVENT);
        list.add(DomainType.REPOSITORY);
        list.add(DomainType.AGGREGATE_ROOT);
        list.add(DomainType.ENTITY);
        list.add(DomainType.VALUE_OBJECT);
        list.add(DomainType.ENUM);
        list.add(DomainType.QUERY_HANDLER);
        list.add(DomainType.READ_MODEL);
        list.add(DomainType.OUTBOUND_SERVICE);
        list.add(DomainType.SERVICE_KIND);
        list.add(DomainType.IDENTITY);
        list.add(DomainType.NON_DOMAIN);
        return list;
    }

    private List<? extends DomainTypeMirror> filterConcreteMirrorsInterfaceAvailable(List<? extends DomainTypeMirror> mirrors) {
        var list = new ArrayList<DomainTypeMirror>();
        if(mirrors != null && mirrors.size() > 0) {
            var mirroredTypes = mirrors.stream().map(m -> m.getTypeName()).toList();
            list.addAll(mirrors);
            for (var mirror : mirrors) {
                for(var interfaceType : mirror.getAllInterfaceTypeNames()){
                    if(mirroredTypes.contains(interfaceType)){
                        list.remove(mirror);
                    }
                }
            }
        }
        return list;
    }
}
