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
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.popover.PopoverVariant;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.dom.Style.Position;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.LineHeight;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.service.AuthenticatedUserService;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.service.SecurityService;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.UploadDialog;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramView;
import io.domainlifecycles.diagramviewer.webapp.views.ProjectView;
import java.util.Comparator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Layout
@CssImport("./styles/diagram-viewer-styles.css")
public class MainLayout extends AppLayout {

    private final static Logger log = LoggerFactory.getLogger(MainLayout.class);
    private static final String DLC_LOGO_LOCATION = "frontend/dlc-logo.png";

    private final ProjectService projectService;
    private final SecurityService securityService;
    private final AuthenticatedUserService authenticatedUserService;
    private final UploadDialog uploadDialog;
    private SideNav sideNav;
    private Registration registration;
    private Popover userInfoPopover;

    public MainLayout(ProjectService projectService,
                      SecurityService securityService, AuthenticatedUserService authenticatedUserService) {

        this.projectService = projectService;
        this.securityService = securityService;
        this.authenticatedUserService = authenticatedUserService;
        this.uploadDialog = new UploadDialog(projectService, securityService);

        addToNavbar(new DrawerToggle(), getDlcLogo());
        createAndAddUserInfoPopoverWithButton();
        buildDrawerContent();
    }

    private void refreshPopover() {
        userInfoPopover.removeAll();
        addPopoverContents();
    }

    private void createAndAddUserInfoPopoverWithButton() {
        Button userInfoPopoverButton = createAndGetUserInfoPopoverButton();
        userInfoPopover = createAndGetUserInfoPopover(userInfoPopoverButton);
        addPopoverContents();
        addToNavbar(userInfoPopoverButton, userInfoPopover);
    }

    private Popover createAndGetUserInfoPopover(Button popOverButton) {
        Popover popover = new Popover();
        popover.setModal(true);
        popover.setOverlayRole("menu");
        popover.setAriaLabel("User menu");
        popover.setTarget(popOverButton);
        popover.setPosition(PopoverPosition.BOTTOM_END);
        popover.addThemeVariants(PopoverVariant.LUMO_NO_PADDING);

        return popover;
    }

    private void addPopoverContents() {
        userInfoPopover.add(createAndGetPopoverUserInfoLayout(), new Hr(), createAndGetPopoverApiKeyLayout());
    }

    private HorizontalLayout createAndGetPopoverUserInfoLayout() {
        HorizontalLayout userInfo = new HorizontalLayout();
        userInfo.getStyle().setPadding("0rem 1rem 0rem");
        userInfo.getThemeList().remove("spacing");

        Avatar popoverAvatar = new Avatar();
        popoverAvatar.setName(securityService.getAuthenticatedUser().getFullName());
        popoverAvatar.getStyle().set("margin", "auto");
        popoverAvatar.getElement().setAttribute("tabindex", "-1");
        popoverAvatar.addThemeVariants(AvatarVariant.LUMO_LARGE);

        VerticalLayout nameLayout = new VerticalLayout();
        nameLayout.getThemeList().remove("spacing");
        Div fullName = new Div(securityService.getAuthenticatedUser().getFullName());
        fullName.getStyle().set("font-weight", "bold");
        Div nickName = new Div(securityService.getAuthenticatedUser().getEmailAddress());
        nameLayout.add(fullName, nickName);

        userInfo.add(popoverAvatar, nameLayout);
        return userInfo;
    }

    private VerticalLayout createAndGetPopoverApiKeyLayout() {
        VerticalLayout apiKeyLayout = new VerticalLayout();
        apiKeyLayout.getThemeList().remove("spacing");
        apiKeyLayout.getStyle().setPadding("0 --var(--lumo-space-m)");

        if(securityService.getAuthenticatedUser().hasApiKey()) {
            Paragraph apiKeyParagraph = new Paragraph("API-Key:");
            apiKeyParagraph.getStyle().setMargin("0");
            apiKeyParagraph.getStyle().setFontWeight("bold");

            Paragraph apiKeyValueParagraph = new Paragraph(securityService.getAuthenticatedUser().getApiKey().toString());
            apiKeyValueParagraph.getStyle().setMargin("0");

            apiKeyLayout.add(apiKeyParagraph, apiKeyValueParagraph);
        } else {
            Button generateApiKeyButton = new Button("Generate API-Key", e -> {
                authenticatedUserService.generateApiKeyForUser(securityService.getAuthenticatedUser());
                refreshPopover();
            });

            generateApiKeyButton.setPrefixComponent(new Icon("vaadin:key-o"));
            generateApiKeyButton.setWidthFull();
            apiKeyLayout.add(generateApiKeyButton);
        }
        return apiKeyLayout;
    }

    private Button createAndGetUserInfoPopoverButton() {
        Avatar avatar = new Avatar();
        avatar.setName(securityService.getAuthenticatedUser().getFullName());
        avatar.getStyle().set("display", "block");
        avatar.getStyle().set("cursor", "pointer");
        avatar.getElement().setAttribute("tabindex", "-1");

        Button button = new Button(avatar);
        button.addThemeVariants(ButtonVariant.LUMO_ICON,
            ButtonVariant.LUMO_TERTIARY_INLINE);
        button.getStyle().set("margin", "var(--lumo-space-s)");
        button.getStyle().set("margin-inline-start", "auto");
        button.getStyle().set("border-radius", "50%");
        return button;
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
        return projectService.getAll(securityService.getAuthenticatedUser())
            .sorted(Comparator.comparing(Project::getCreatedAt))
            .map(project -> {
                SideNavItem parentSideNavItem = new SideNavItem(project.getName(), ProjectView.class, new RouteParameters(Map.of("projectName", project.getName())));
                parentSideNavItem.getStyle().setHeight(LineHeight.MEDIUM);

                project.getDiagrams()
                    .forEach(diagram -> {
                        SideNavItem sideNavItem = new SideNavItem(diagram.getFileName(), DiagramView.class,
                            new RouteParameters(Map.of("projectName", project.getName(), "diagramName",
                                diagram.getFileName())));

                        sideNavItem.getStyle().setLineHeight(LineHeight.SMALL);
                        parentSideNavItem.addItem(sideNavItem);
                    });

                return parentSideNavItem;
            })
            .toArray(SideNavItem[]::new);
    }

    private void refreshSideNavLinks() {
        sideNav.removeAll();
        sideNav.addItem(createSideNavLinks());
    }

    private Component getDlcLogo() {
        Image dlcLogo = new Image(DLC_LOGO_LOCATION, "DLC Logo");
        dlcLogo.setMaxHeight("35px");
        dlcLogo.getStyle().setPosition(Position.ABSOLUTE);
        dlcLogo.getStyle().setTop("50%");
        dlcLogo.getStyle().setTransform("translateY(-50%)");

        return new Anchor("/", dlcLogo);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        registration =
            ComponentUtil.addListener(
                attachEvent.getUI(),
                DiagramsOrProjectsChangedEvent.class,
                event -> {
                    refreshSideNavLinks();
                }
            );
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        registration.remove();
    }
}
