package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.domainlifecycles.diagramviewer.model.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.service.DiagramDirectoryService;
import io.domainlifecycles.diagramviewer.webapp.components.various.cards.DiagramCardGridContainer;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.layout.MainLayout;
import jakarta.annotation.security.PermitAll;

@Route(value = "/directory/:" + DiagramDirectoryView.DIAGRAM_DIRECTORY_NAME_ROUTE_PARAMETER, layout = MainLayout.class)
@PageTitle("DLC | Directory Viewer")
@PermitAll
public class DiagramDirectoryView extends FlexLayout implements BeforeEnterObserver {

    public static final String DIAGRAM_DIRECTORY_NAME_ROUTE_PARAMETER = "diagramDirectoryName";

    private final DiagramDirectoryService diagramDirectoryService;

    private DiagramDirectory diagramDirectory;
    private Project project;
    private String diagramDirectoryName;

    public DiagramDirectoryView(DiagramDirectoryService diagramDirectoryService) {

        this.diagramDirectoryService = diagramDirectoryService;

        setSizeFull();
        setFlexDirection(FlexDirection.COLUMN);
        setId("diagram-directory-viewer");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        diagramDirectoryName = event.getRouteParameters().get(DIAGRAM_DIRECTORY_NAME_ROUTE_PARAMETER).orElseThrow();
        refreshPage();
    }

    private void setDiagramDirectoryAndProject() {
        diagramDirectory = diagramDirectoryService.getByName(diagramDirectoryName);
        project = diagramDirectory.getProject();
    }

    private void addPageContents() {
        add(createAndGetNameAndDeleteButtonLayout());
        Scroller scroller = new Scroller(new DiagramCardGridContainer(diagramDirectoryService, project, diagramDirectory.getDiagrams()));
        add(scroller);
    }

    private void refreshPage() {
        setDiagramDirectoryAndProject();
        removeAll();
        addPageContents();
    }

    private HorizontalLayout createAndGetNameAndDeleteButtonLayout() {
        HorizontalLayout horizontalNameAndEditButtonAndReuploadButtonLayout = new HorizontalLayout();
        horizontalNameAndEditButtonAndReuploadButtonLayout.add(new H2(diagramDirectory.getName()), getDeleteDirectoryButton());

        return horizontalNameAndEditButtonAndReuploadButtonLayout;
    }

    private Button getDeleteDirectoryButton() {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Delete Directory");
        confirmDialog.setText(String.format(
            "Are you sure you want to delete directory '%s'?", diagramDirectory.getName()));

        confirmDialog.setCancelable(true);

        confirmDialog.setConfirmText("Delete");
        confirmDialog.setConfirmButtonTheme("error primary");
        confirmDialog.addConfirmListener(event -> {
            diagramDirectoryService.delete(diagramDirectory);
            confirmDialog.close();
            UI.getCurrent().navigate(DefaultView.class);
            ComponentUtil.fireEvent(UI.getCurrent(), new DiagramsOrProjectsChangedEvent(this, false));
        });

        Button deleteDirectoryButton = new Button("Delete", new Icon("vaadin:trash"));
        deleteDirectoryButton.getStyle().set("cursor", "pointer");
        deleteDirectoryButton.getElement().getStyle().set("margin-left", "auto");
        deleteDirectoryButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteDirectoryButton.addClickListener(e -> confirmDialog.open());
        return deleteDirectoryButton;
    }
}
