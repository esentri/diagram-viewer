package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.RouteParameters;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.views.ProjectView;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EditProjectDialog extends Dialog {

    private final Project project;
    private final ProjectService projectService;
    private final Binder<Project> projectConfigurationBinder;

    public EditProjectDialog(Project project, ProjectService projectService) {
        this.project = project;
        this.projectService = projectService;
        this.projectConfigurationBinder = new Binder<>(Project.class);

        setHeaderTitle("Edit Project");
        setWidth("30%");
        setHeight("50%");

        add(createDialogLayout());
        projectConfigurationBinder.readBean(project);

        getFooter().add(createSaveButton());
        getFooter().add(createCancelButton());
    }

    private Button createSaveButton() {
        Button saveButton = new Button("Save");

        saveButton.addClickListener(e -> {
            projectConfigurationBinder.writeBeanIfValid(project);
            projectService.update(project);
            close();
            UI.getCurrent().navigate(ProjectView.class, new RouteParameters(Map.of("projectName", project.getName())));
            ComponentUtil.fireEvent(UI.getCurrent(), new DiagramsOrProjectsChangedEvent(this, false));
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return saveButton;
    }

    private Button createCancelButton() {
        return new Button("Cancel", e -> close());
    }

    private FormLayout createDialogLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        TextField projectNameTextField = new TextField();
        projectNameTextField.setWidthFull();
        formLayout.addFormItem(projectNameTextField, "Name");
        projectConfigurationBinder.forField(projectNameTextField).bind(Project::getName, Project::setName);

        TextField boundedContextPackagesTextField = new TextField();
        boundedContextPackagesTextField.setWidthFull();
        formLayout.addFormItem(boundedContextPackagesTextField, "Bounded Context Packages (comma separated)");
        projectConfigurationBinder.forField(boundedContextPackagesTextField)
            .bind(project -> mapBoundedContextPackages(project.getBoundedContextPackages()), this::setBoundedContextPackages);

        return formLayout;
    }

    private void setBoundedContextPackages(Project project, String selectedContextPackages) {
        project.setBoundedContextPackages(Arrays.stream(selectedContextPackages.split(",")).toList());
    }

    private String mapBoundedContextPackages(List<String> boundedContextPackages) {
        return String.join(",", boundedContextPackages);
    }
}
