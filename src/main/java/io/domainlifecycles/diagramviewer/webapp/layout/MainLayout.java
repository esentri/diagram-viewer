package io.domainlifecycles.diagramviewer.webapp.layout;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.popover.PopoverVariant;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.files.DirectoryWatcher;
import io.domainlifecycles.diagramviewer.security.SecurityService;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.service.UserService;
import io.domainlifecycles.diagramviewer.session.DomainModelSessionStorage;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.UploadDialog;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsChangedEventListener;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramView;
import io.domainlifecycles.diagramviewer.webapp.views.ProjectView;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

@Layout
@CssImport("./styles/diagram-viewer-styles.css")
@CssImport("./styles/google-styles.css")
public class MainLayout extends AppLayout {

    private final static Logger log = LoggerFactory.getLogger(MainLayout.class);
    private static final String DLC_LOGO_LOCATION = "frontend/dlc-logo.png";

    private final SecurityService securityService;
    private final ProjectService projectService;
    private final DomainModelSessionStorage sessionStorage;
    private final UploadDialog uploadDialog;
    private SideNav sideNav;
    private Registration registration;

    public MainLayout(SecurityService securityService,
                      ProjectService projectService,
                      DomainModelSessionStorage sessionStorage) {

        this.securityService = securityService;
        this.projectService = projectService;
        this.sessionStorage = sessionStorage;
        this.uploadDialog = new UploadDialog(projectService, sessionStorage);

        addToNavbar(new DrawerToggle(), getDlcLogo());
        createAndAddUserInfoPopover();
        buildDrawerContent();

        addListener(DiagramsChangedEvent.class, (DiagramsChangedEventListener<DiagramsChangedEvent>) event -> refreshSideNavLinks());
    }

    private void createAndAddUserInfoPopover() {
        Avatar avatar = new Avatar();
        avatar.getStyle().set("display", "block");
        avatar.getStyle().set("cursor", "pointer");
        avatar.getElement().setAttribute("tabindex", "-1");

        Button button = new Button(avatar);
        button.addThemeVariants(ButtonVariant.LUMO_ICON,
            ButtonVariant.LUMO_TERTIARY_INLINE);
        button.getStyle().set("margin", "var(--lumo-space-s)");
        button.getStyle().set("margin-inline-start", "auto");
        button.getStyle().set("border-radius", "50%");

        Popover popover = new Popover();
        popover.setModal(true);
        popover.setOverlayRole("menu");
        popover.setAriaLabel("User menu");
        popover.setTarget(button);
        popover.setPosition(PopoverPosition.BOTTOM_END);
        popover.addThemeVariants(PopoverVariant.LUMO_NO_PADDING);

        HorizontalLayout userInfo = new HorizontalLayout();
        userInfo.getStyle().setPadding("0rem 1rem 0rem");
        userInfo.getThemeList().remove("spacing");

        Avatar popoverAvatar = new Avatar();
        popoverAvatar.getStyle().set("margin", "auto");
        popoverAvatar.getElement().setAttribute("tabindex", "-1");
        popoverAvatar.addThemeVariants(AvatarVariant.LUMO_LARGE);

        VerticalLayout nameLayout = new VerticalLayout();
        nameLayout.getThemeList().remove("spacing");
        Div fullName = new Div(sessionStorage.getAuthenticatedUser().getFullName());
        fullName.getStyle().set("font-weight", "bold");
        Div nickName = new Div(sessionStorage.getAuthenticatedUser().getEmailAddress());
        nameLayout.add(fullName, nickName);

        userInfo.add(popoverAvatar, nameLayout);
        popover.add(userInfo);

        addToNavbar(button, popover);
    }

    private void buildDrawerContent() {
        Scroller scroller = new Scroller(getSideNav());
        scroller.setClassName(LumoUtility.Padding.SMALL);

        Button uploadButton = new Button("Upload", new Icon("vaadin:cloud-upload-o"));
        uploadButton.addClickListener(e -> uploadDialog.open());

        addToDrawer(scroller, uploadButton);
    }

    private SideNav getSideNav() {
        sideNav = new SideNav();
        sideNav.addItem(createSideNavLinks());
        return sideNav;
    }

    private SideNavItem[] createSideNavLinks() {
        return projectService.getAll(buildPath(sessionStorage.getTargetsLocation()), sessionStorage.getAuthenticatedUser())
            .map(project -> {
                SideNavItem parentSideNavItem = new SideNavItem(project.getProjectNameFull(), ProjectView.class, new RouteParameters(Map.of("projectName", project.getProjectNameClean())));

                project.getDiagrams()
                    .forEach(diagram -> {
                        SideNavItem sideNavItem = new SideNavItem(diagram.getFileName(), DiagramView.class,
                            new RouteParameters(Map.of("projectName", project.getProjectNameClean(), "diagramName",
                                diagram.getFileName())));

                        parentSideNavItem.addItem(sideNavItem);
                        initWatcherService(diagram.getFullAbsoluteLocationPath());
                    });

                return parentSideNavItem;
            })
            .toArray(SideNavItem[]::new);
    }

    /**
     * Adding a watcher service on the diagram's directory allows an asynchronous refresh of the diagram zoom
     * component as soon as a new diagram has been rendered, for example when some styling option has been changed
     * in the UI.
     *
     * @param absolutePathToDiagram the absolute path to the diagram's directory
     */
    private void initWatcherService(final String absolutePathToDiagram) {
        Path directoryToWatch;

        directoryToWatch = buildPath(absolutePathToDiagram);

        DirectoryWatcher.onDirectoryChange(directoryToWatch,
            (evt) -> {
                log.debug(String.format("Noticed change in watched diagram's directory '%s'. Refreshing Sidenav.", directoryToWatch));
                this.getUI().ifPresent(ui -> ui.access(this::refreshSideNavLinks));
            }
        );
    }

    private void refreshSideNavLinks() {
        sideNav.removeAll();
        sideNav.addItem(createSideNavLinks());
    }

    private Component getDlcLogo() {
        Image dlcLogo = new Image(DLC_LOGO_LOCATION, "DLC Logo");
        dlcLogo.setMaxHeight("60px");

        return new Anchor("/", dlcLogo);
    }

    private static Path buildPath(String absolutePath) {
        Path directoryToWatch;
        try {
            directoryToWatch = Path.of(absolutePath);
        } catch (InvalidPathException e) {
            throw DiagramViewerException.fail(
                String.format("Specified path '%s' is not a directory.", absolutePath), e);
        }
        return directoryToWatch;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        registration =
            ComponentUtil.addListener(
                attachEvent.getUI(),
                DiagramsChangedEvent.class,
                event -> refreshSideNavLinks()
            );
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        registration.remove();
    }
}
