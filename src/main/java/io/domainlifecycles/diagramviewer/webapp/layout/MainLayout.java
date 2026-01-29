/*
 *     ________  .__
 *     \______ \ |__|____     ________________    _____
 *      |    |  \|  \__  \   / ___\_  __ \__  \  /     \
 *      |    |   \  |/ __ \_/ /_/  >  | \// __ \|  Y Y  \
 *     /_______  /__(____  /\___  /|__|  (____  /__|_|  /
 *             \/        \//_____/            \/      \/
 *     ____   ____.__
 *     \   \ /   /|__| ______  _  __ ___________
 *      \   Y   / |  |/ __ \ \/ \/ // __ \_  __ \
 *       \     /  |  \  ___/\     /\  ___/|  | \/
 *        \___/   |__|\___  >\/\_/  \___  >__|
 *                        \/            \/
 *
 *  Copyright 2019-2025 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.domainlifecycles.diagramviewer.webapp.layout;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
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
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.service.SecurityService;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.UploadDialog;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramDirectoryView;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramView;
import io.domainlifecycles.diagramviewer.webapp.views.ProjectView;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;

@Layout
@CssImport("./styles/diagram-viewer-styles.css")
public class MainLayout extends AppLayout {

    private static final String DLC_LOGO_LOCATION = "frontend/dlc-logo.png";

    private final ProjectService projectService;
    private final SecurityService securityService;
    private final UploadDialog uploadDialog;
    private SideNav sideNav;
    private Registration registration;
    private Popover userInfoPopover;

    private final boolean jarUploadEnabled;

    public MainLayout(@Value("${jar.upload.enabled}") boolean jarUploadEnabled,
                      ProjectService projectService,
                      SecurityService securityService) {
        this.jarUploadEnabled = jarUploadEnabled;
        this.projectService = projectService;
        this.securityService = securityService;
        if(jarUploadEnabled){
            this.uploadDialog = new UploadDialog(projectService, securityService);
        }else{
            this.uploadDialog = null;
        }

        addToNavbar(new DrawerToggle(), getDlcLogo());
        createAndAddUserInfoPopoverWithButton();
        buildDrawerContent();
    }

    private void createAndAddUserInfoPopoverWithButton() {
        final AppUser appUser = securityService.getCurrentlySignedInUser();

        Button userInfoPopoverButton = createAndGetUserInfoPopoverButton(appUser);
        userInfoPopover = createAndGetUserInfoPopover(userInfoPopoverButton);
        addPopoverContents(appUser);
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

    private void addPopoverContents(AppUser appUser) {
        userInfoPopover.add(createAndGetPopoverUserInfoLayout(appUser), new Hr(),
            createAndGetPopoverApiKeyLayout(appUser), new Hr(), createAndGetSignOutButtonLayout());
    }

    private HorizontalLayout createAndGetPopoverUserInfoLayout(AppUser appUser) {
        HorizontalLayout userInfo = new HorizontalLayout();
        userInfo.getStyle().setPadding("0rem 1rem 0rem");
        userInfo.setSpacing(false);

        Avatar popoverAvatar = new Avatar();
        popoverAvatar.setName(String.join(" ", appUser.getFirstName(), appUser.getLastName()));
        popoverAvatar.getStyle().set("margin", "auto");
        popoverAvatar.getElement().setAttribute("tabindex", "-1");
        popoverAvatar.addThemeVariants(AvatarVariant.LUMO_LARGE);

        VerticalLayout nameLayout = new VerticalLayout();
        nameLayout.setSpacing(false);
        Div fullName = new Div(String.join(" ", appUser.getFirstName(), appUser.getLastName()));
        fullName.getStyle().set("font-weight", "bold");
        Div nickName = new Div(appUser.getEmailAddress());
        nameLayout.add(fullName, nickName);

        userInfo.add(popoverAvatar, nameLayout);
        return userInfo;
    }

    private VerticalLayout createAndGetPopoverApiKeyLayout(AppUser appUser) {
        VerticalLayout apiKeyLayout = new VerticalLayout();
        apiKeyLayout.setSpacing(false);

        String apiKey = appUser.getApiKey().toString();

        Button copyApiKeyButton = new Button("Copy API-Key", e -> {
            UI.getCurrent().getPage()
                .executeJs("navigator.clipboard.writeText($0)", apiKey);
            Notification.show("Your API-Key has been copied to clipboard.");
        });

        copyApiKeyButton.getStyle().set("cursor", "pointer");
        copyApiKeyButton.setPrefixComponent(new Icon("vaadin:key"));
        copyApiKeyButton.setWidthFull();

        apiKeyLayout.add(copyApiKeyButton);
        return apiKeyLayout;
    }

    private Button createAndGetUserInfoPopoverButton(AppUser appUser) {
        Avatar avatar = new Avatar();
        avatar.setName(String.join(" ", appUser.getFirstName(), appUser.getLastName()));
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

    private VerticalLayout createAndGetSignOutButtonLayout() {
        VerticalLayout signOutButtonLayout = new VerticalLayout();

        Button signOutButton = new Button("Sign Out");
        signOutButton.getStyle().set("cursor", "pointer");
        signOutButton.addThemeVariants(ButtonVariant.LUMO_WARNING);
        signOutButton.setWidthFull();
        signOutButton.getStyle().setMargin("0");
        signOutButton.addClickListener(e -> signOutUser());

        signOutButtonLayout.add(signOutButton);
        return signOutButtonLayout;
    }

    private void signOutUser() {
        UI.getCurrent().getPage().setLocation("/logout");
    }

    private void buildDrawerContent() {
        Scroller scroller = new Scroller(getSideNav());
        scroller.setClassName(LumoUtility.Padding.SMALL);

        addToDrawer(scroller);

        if(jarUploadEnabled) {
            Button uploadButton = new Button("Upload", new Icon("vaadin:cloud-upload-o"));
            uploadButton.addClickListener(e -> uploadDialog.open());
            uploadButton.getStyle().set("cursor", "pointer");
            addToDrawer(uploadButton);
        }
    }

    private SideNav getSideNav() {
        sideNav = new SideNav();
        sideNav.addItem(createSideNavLinks());
        return sideNav;
    }

    private SideNavItem[] createSideNavLinks() {
        return projectService.getAllAssignedSortedByCreationDate(securityService.getCurrentlySignedInUser())
            .stream()
            .map(this::createAndGetProjectSideNavItem)
            .toArray(SideNavItem[]::new);
    }

    private SideNavItem createAndGetProjectSideNavItem(Project project) {
        SideNavItem projectSideNavItem = new SideNavItem(
            project.getName(), ProjectView.class, new RouteParameters(Map.of(ProjectView.PROJECT_NAME_ROUTE_PARAMETER, project.getName())));
        projectSideNavItem.getStyle().setHeight(LineHeight.MEDIUM);

        project.getDiagramDirectories()
            .stream().sorted(Comparator.comparing(DiagramDirectory::getCreatedAt))
                .forEach(diagramDirectory -> {
                    SideNavItem directorySideNavItem = createAndGetDiagramDirectorySideNavItem(project, diagramDirectory);
                    projectSideNavItem.addItem(directorySideNavItem);
                });

        createAndAddChildDiagramSideNavItems(project, projectSideNavItem, project.getDiagramsWithoutDirectory());

        return projectSideNavItem;
    }

    private SideNavItem createAndGetDiagramDirectorySideNavItem(Project project, DiagramDirectory diagramDirectory) {
        SideNavItem directorySideNavItem = new SideNavItem(diagramDirectory.getName(), DiagramDirectoryView.class,
            new RouteParameters(Map.of(DiagramDirectoryView.DIAGRAM_DIRECTORY_NAME_ROUTE_PARAMETER, diagramDirectory.getName())));

        createAndAddChildDiagramSideNavItems(project, directorySideNavItem, diagramDirectory.getDiagrams());

        directorySideNavItem.getStyle().setLineHeight(LineHeight.SMALL);
        return directorySideNavItem;
    }

    private void createAndAddChildDiagramSideNavItems(Project project, SideNavItem parentSideNavItem, Set<Diagram> diagrams) {
        diagrams
            .stream().sorted(Comparator.comparing(Diagram::getCreatedAt))
            .forEach(diagram -> {
                SideNavItem sideNavItem = new SideNavItem(diagram.getName(), DiagramView.class,
                    new RouteParameters(
                        Map.of(ProjectView.PROJECT_NAME_ROUTE_PARAMETER, project.getName(), "diagramName",
                        diagram.getName())));

                sideNavItem.getStyle().setLineHeight(LineHeight.SMALL);
                parentSideNavItem.addItem(sideNavItem);
            });
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
                event -> refreshSideNavLinks()
            );
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        registration.remove();
    }
}
