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

package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.shared.Registration;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.webapp.events.ProjectUsersChangedEvent;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShareProjectDialog extends Dialog {

    private final ProjectService projectService;
    private final Project project;
    private Grid<AppUser> userGrid;
    private Registration registration;

    public ShareProjectDialog(ProjectService projectService, Project project) {
        this.project = project;
        this.projectService = projectService;

        setHeaderTitle("Share Project");

        setWidth("40%");
        setHeight("60%");

        add(createDialogLayout());
        getFooter().add(createCloseButton());
    }

    private Button createCloseButton() {
        return new Button("Close", e -> close());
    }

    private VerticalLayout createDialogLayout() {
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSizeFull();

        AddUserDialog addUserDialog = new AddUserDialog(project, projectService);
        dialogLayout.add(new Button("Add User...", new Icon("vaadin:plus"), e -> addUserDialog.open()));

        userGrid = new Grid<>();
        userGrid.setWidthFull();
        userGrid.setAllRowsVisible(true);
        userGrid.setSelectionMode(Grid.SelectionMode.NONE);
        userGrid.getStyle().setBorder("none");
        userGrid.getStyle().setBoxShadow("none");
        userGrid.setItems(getRegisteredAndInvitedUsersForProject());

        userGrid.addColumn(createEmployeeRenderer());
        userGrid.addComponentColumn(this::createAndGetUnassignButtonWithConfirmDialog).setTextAlign(ColumnTextAlign.END);

        dialogLayout.add(userGrid);
        return dialogLayout;
    }

    private Set<AppUser> getRegisteredAndInvitedUsersForProject() {
        Set<AppUser> assignedAppUsers = project.getAssignedUsers();
        return new HashSet<>(assignedAppUsers);
    }

    private void refreshUsers() {
        userGrid.setItems(getRegisteredAndInvitedUsersForProject());
    }

    /**
     * Displays the Full name and Email when both are given. If the full name is null, only the email address should be
     * displayed.
     *
     * @return Rendered HTML
     */
    private Renderer<AppUser> createEmployeeRenderer() {
        return LitRenderer.<AppUser> of(
                      "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">"
                    + "  <vaadin-avatar name=\"${item.displayName}\"></vaadin-avatar>"
                    + "  <vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">"
                    + "     <span>${item.displayName}</span>"
                    + "     <span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\" ?hidden=${!item.showEmail}>"
                    + "         ${item.email}"
                    + "     </span>"
                    + "  </vaadin-vertical-layout>"
                    + "</vaadin-horizontal-layout>")
            .withProperty("displayName", user -> {
                String fullName = String.join(" ", user.getFirstName(), user.getLastName());
                return (fullName != null && !fullName.isEmpty()) ? fullName : user.getEmailAddress();
            })
            .withProperty("email", AppUser::getEmailAddress)
            .withProperty("showEmail", user -> {
                String fullName = String.join(" ", user.getFirstName(), user.getLastName());
                return !fullName.isEmpty();
            });
    }

    private Button createAndGetUnassignButtonWithConfirmDialog(final AppUser user) {
        if((user != null)
            && Objects.equals(user.getId(),
                project.getCreator().getId())) {
            return null;
        }

        ConfirmDialog confirmDialog = getUnassignConfirmDialog(user);

        Button unassignButton = new Button(new Icon("vaadin:trash"), e -> confirmDialog.open());
        unassignButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        return unassignButton;
    }

    private ConfirmDialog getUnassignConfirmDialog(AppUser user) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Unassign User");
        confirmDialog.setText(String.format(
            "Are you sure you want to unassign user '%s' from your project?", user.getEmailAddress()));

        confirmDialog.setCancelable(true);

        confirmDialog.setConfirmText("Unassign");
        confirmDialog.addConfirmListener(event -> {
            projectService.unassignUser(project, user);
            ComponentUtil.fireEvent(UI.getCurrent(), new ProjectUsersChangedEvent(this, false));
            confirmDialog.close();
        });
        return confirmDialog;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        registration =
            ComponentUtil.addListener(
                attachEvent.getUI(),
                ProjectUsersChangedEvent.class,
                event -> refreshUsers()
            );
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        registration.remove();
    }
}
