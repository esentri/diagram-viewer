package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import io.domainlifecycles.diagramviewer.model.AuthenticatedUser;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.TemporaryUser;
import io.domainlifecycles.diagramviewer.model.User;
import io.domainlifecycles.diagramviewer.service.AuthenticatedUserService;
import io.domainlifecycles.diagramviewer.service.TemporaryUserService;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ManageUsersDialog extends Dialog {

    private final Project project;
    private final AuthenticatedUserService authenticatedUserService;
    private final TemporaryUserService temporaryUserService;

    public ManageUsersDialog(Project project, AuthenticatedUserService authenticatedUserService, TemporaryUserService temporaryUserService) {
        this.project = project;
        this.authenticatedUserService = authenticatedUserService;
        this.temporaryUserService = temporaryUserService;

        setHeaderTitle("Manage Users");

        setWidth("40%");
        setHeight("60%");

        add(createDialogLayout());
        getFooter().add(createApplyButton());
        getFooter().add(createCancelButton());
    }

    private Button createApplyButton() {
        Button applyButton = new Button("Apply");

        applyButton.addClickListener(e -> {
            close();
        });

        applyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return applyButton;
    }

    private Button createCancelButton() {
        return new Button("Cancel", e -> close());
    }

    private VerticalLayout createDialogLayout() {
        VerticalLayout dialogLayout = new VerticalLayout();

        dialogLayout.add(new Button("Add User...", new Icon("vaadin:plus")));

        Grid<User> userGrid = new Grid<>();
        userGrid.setWidthFull();
        userGrid.setSelectionMode(Grid.SelectionMode.NONE);
        userGrid.getStyle().setBorder("none");
        userGrid.getStyle().setBoxShadow("none");
        userGrid.setItems(getAuthenticatedAndTemporaryUsersForProject());

        userGrid.addColumn(createEmployeeRenderer());
        userGrid.addComponentColumn(this::createAndGetUnassignButtonWithConfirmDialog);

        dialogLayout.add(userGrid);
        return dialogLayout;
    }

    private List<User> getAuthenticatedAndTemporaryUsersForProject() {
        List<AuthenticatedUser> assignedAuthenticatedUsers = project.getAssignedAuthenticatedUsers();
        List<TemporaryUser> assignedTemporaryUsers = project.getAssignedTemporaryUsers();

        return Stream.concat(assignedAuthenticatedUsers.stream(), assignedTemporaryUsers.stream())
            .collect(Collectors.toList());
    }

    private static Renderer<User> createEmployeeRenderer() {
        return LitRenderer.<User> of(
                "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">"
                    + "  <vaadin-avatar name=\"${item.fullName}\"></vaadin-avatar>"
                    + "  <vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">"
                    + "    <span> ${item.fullName} </span>"
                    + "    <span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
                    + "      ${item.email}" + "    </span>"
                    + "  </vaadin-vertical-layout>"
                    + "</vaadin-horizontal-layout>")
            .withProperty("fullName", User::getFullName)
            .withProperty("email", User::getEmailAddress);
    }

    private Button createAndGetUnassignButtonWithConfirmDialog(final User user) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Unassign User");
        dialog.setText(String.format(
            "Are you sure you want to unassign user '%s' from your project?", user.getEmailAddress()));

        dialog.setCancelable(true);

        dialog.setConfirmText("Unassign");
        dialog.addConfirmListener(event -> {
            // TODO: REMOVE USER FROM PROJECT
        });

        return new Button(new Icon("vaadin:trash"), e -> dialog.open());
    }
}
