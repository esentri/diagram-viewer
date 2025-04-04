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
import io.domainlifecycles.diagramviewer.model.AuthenticatedUser;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.TemporaryUser;
import io.domainlifecycles.diagramviewer.model.User;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.webapp.events.ProjectUsersChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.events.ProjectUsersChangedEventListener;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ManageUsersDialog extends Dialog {

    private final Project project;
    private final ProjectService projectService;
    private Grid<User> userGrid;
    private Registration registration;

    public ManageUsersDialog(Project project, ProjectService projectService) {
        this.project = project;
        this.projectService = projectService;

        setHeaderTitle("Manage Users");

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

        AddUserDialog addUserDialog = new AddUserDialog(project, projectService);
        dialogLayout.add(new Button("Add User...", new Icon("vaadin:plus"), e -> addUserDialog.open()));

        userGrid = new Grid<>();
        userGrid.setWidthFull();
        userGrid.setSelectionMode(Grid.SelectionMode.NONE);
        userGrid.getStyle().setBorder("none");
        userGrid.getStyle().setBoxShadow("none");
        userGrid.setItems(getAuthenticatedAndTemporaryUsersForProject());

        userGrid.addColumn(createEmployeeRenderer());
        userGrid.addComponentColumn(this::createAndGetUnassignButtonWithConfirmDialog).setTextAlign(ColumnTextAlign.END);

        dialogLayout.add(userGrid);
        return dialogLayout;
    }

    private List<User> getAuthenticatedAndTemporaryUsersForProject() {
        List<AuthenticatedUser> assignedAuthenticatedUsers = project.getAssignedAuthenticatedUsers();
        List<TemporaryUser> assignedTemporaryUsers = project.getAssignedTemporaryUsers();

        return Stream.concat(assignedAuthenticatedUsers.stream(), assignedTemporaryUsers.stream())
            .collect(Collectors.toList());
    }

    private void refreshUsers() {
        userGrid.setItems(getAuthenticatedAndTemporaryUsersForProject());
    }

    /**
     * Displays the Full name and Email when both are given. If the full name is null, only the email address should be
     * displayed.
     *
     * @return Rendered HTML
     */
    private Renderer<User> createEmployeeRenderer() {
        return LitRenderer.<User> of(
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
                String fullName = user.getFullName();
                return (fullName != null && !fullName.isEmpty()) ? fullName : user.getEmailAddress();
            })
            .withProperty("email", User::getEmailAddress)
            .withProperty("showEmail", user -> {
                String fullName = user.getFullName();
                return (fullName != null && !fullName.isEmpty());
            });
    }

    private Button createAndGetUnassignButtonWithConfirmDialog(final User user) {
        if((user instanceof AuthenticatedUser)
            && Objects.equals(((AuthenticatedUser) user).getAuthenticatedUserId(),
                project.getCreator().getAuthenticatedUserId())) {
            return null;
        }

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

        Button unassignButton = new Button(new Icon("vaadin:trash"), e -> confirmDialog.open());
        unassignButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        return unassignButton;
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
