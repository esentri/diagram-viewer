package io.domainlifecycles.diagramviewer.webapp.views;

import com.flowingcode.vaadin.addons.zoomist.Zoomist;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.domainlifecycles.diagramviewer.rest.ResourceController;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.components.DiagramZoomComponent;
import io.domainlifecycles.diagramviewer.webapp.layout.MainView;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.atmosphere.interceptor.AtmosphereResourceStateRecovery.B;
import org.springframework.beans.factory.annotation.Value;

@Route(value = "/", layout = MainView.class)
@PageTitle("DLC | Diagram Viewer")
public class DiagramViewerView extends FlexLayout {

    @Value("${diagrams.location}")
    private String diagramDirectory;
    private final TabSheet tabSheet;

    public DiagramViewerView() {
        this.setSizeFull();
        this.setClassName("diagram-viewer");

        addCustomizationButtons();

        tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        add(tabSheet);
    }

    private void addCustomizationButtons() {
        Button colorButton = new Button(new Icon("vaadin:paintbrush"));
        Button fontButton = new Button(new Icon("vaadin:font"));
        Button showFieldsButton = new Button(new Icon("vaadin:input"));
        Button showBuildingBlocks = new Button(new Icon("vaadin:connect-o"));

        FlexLayout buttonContainer = new FlexLayout(colorButton, fontButton, showFieldsButton, showBuildingBlocks);
        buttonContainer.setJustifyContentMode(JustifyContentMode.CENTER);

        buttonContainer.setFlexDirection(FlexDirection.COLUMN);
        add(buttonContainer);
    }

    // PostConstruct so property is evaluated
    @PostConstruct
    public void addTabs() {
        Set<String> diagramFileNames = FileIOUtils.getFileNamesInDiagramDirectory(diagramDirectory);

        diagramFileNames.forEach(diagramFileName -> {
            DiagramZoomComponent zoomComponent =
                new DiagramZoomComponent(ResourceController.RESOURCES_API_PATH + "/" + diagramFileName);

            FlexLayout zoomistContainer = generateZoomComponentContainer(zoomComponent);
            tabSheet.add(diagramFileName, zoomistContainer);
        });
    }

    private FlexLayout generateZoomComponentContainer(final DiagramZoomComponent zoomComponent) {
        FlexLayout zoomistContainer = new FlexLayout();
        zoomistContainer.setFlexGrow(1, zoomComponent);
        zoomistContainer.add(zoomComponent);
        zoomistContainer.setMinHeight("0%");
        return zoomistContainer;
    }
}
