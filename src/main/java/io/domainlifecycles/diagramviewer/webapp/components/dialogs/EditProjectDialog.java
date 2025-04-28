package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ErrorLevel;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.RouteParameters;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.Styling;
import io.domainlifecycles.diagramviewer.webapp.components.various.PackageSelectChipField;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.views.ProjectView;
import io.domainlifecycles.mirror.api.BoundedContextMirror;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;

public class EditProjectDialog extends Dialog {

    private final Project project;
    private final ProjectService projectService;
    private final Binder<UploadOptions> binder;
    private final UploadOptions uploadOptions;

    private Button saveButton;

    public EditProjectDialog(Project project, ProjectService projectService) {
        this.project = project;
        this.projectService = projectService;
        this.binder = new Binder<>();

        uploadOptions = new UploadOptions(project.getName(), mapPackageNames());

        setHeaderTitle("Edit Project");
        setWidth("30%");
        setHeight("50%");

        getFooter().add(createSaveButton());
        getFooter().add(createCancelButton());
        add(createDialogLayout());
        binder.readBean(uploadOptions);
    }

    private Button createSaveButton() {
        saveButton = new Button("Save");

        saveButton.addClickListener(e -> {
            binder.writeBeanIfValid(uploadOptions);
            projectService.update(project, uploadOptions.getProjectName(), uploadOptions.getBoundedContextPackages());
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
        binder.forField(projectNameTextField)
            .asRequired("Project name may not be empty")
            .bind(UploadOptions::getProjectName, UploadOptions::setProjectName);
        binder.addStatusChangeListener(event -> saveButton.setEnabled(binder.isValid()));

        formLayout.addFormItem(projectNameTextField, "Name");

        PackageSelectChipField packageSelectChipField = new PackageSelectChipField(mapPackageNames());
        packageSelectChipField.setWidthFull();
        binder.forField(packageSelectChipField)
            .asRequired((value, context) -> {
                if (value == null || value.isEmpty())
                    return ValidationResult.create("Bounded Contexts may not be empty", ErrorLevel.ERROR);
                return ValidationResult.ok();
            })
            .bind(UploadOptions::getBoundedContextPackages, UploadOptions::setBoundedContextPackages);
        binder.addStatusChangeListener(event -> saveButton.setEnabled(binder.isValid()));
        formLayout.addFormItem(packageSelectChipField, "Context-Packages");

        return formLayout;
    }

    private Set<String> mapPackageNames() {
        return project.getDomainModel().boundedContextMirrors().stream().map(
            BoundedContextMirror::getPackageName).collect(
            Collectors.toSet());
    }

    @Data
    @AllArgsConstructor
    private static class UploadOptions {
        private String projectName;
        private Set<String> boundedContextPackages;
    }
}
