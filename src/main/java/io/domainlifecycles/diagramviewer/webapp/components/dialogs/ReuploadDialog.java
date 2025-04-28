package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ErrorLevel;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.router.RouteParameters;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.webapp.components.various.PackageSelectChipField;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.views.ProjectView;
import io.domainlifecycles.mirror.api.BoundedContextMirror;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;

public class ReuploadDialog extends Dialog {

    private final ProjectService projectService;
    private final Binder<UploadOptions> binder;
    private final Project project;
    private final UploadOptions uploadOptions;

    private Button uploadButton;
    private InputStream fileInputStream;

    public ReuploadDialog(Project project, ProjectService projectService) {
        this.project = project;
        this.projectService = projectService;
        this.binder = new Binder<>();

        uploadOptions = new UploadOptions(project.getName(),
            project.getDomainModel().boundedContextMirrors().stream()
                .map(BoundedContextMirror::getPackageName).collect(Collectors.toSet()));

        setHeaderTitle("Reupload Project");
        setWidth("30%");
        setHeight("50%");

        getFooter().add(createUploadButton());
        getFooter().add(createCancelButton());
        add(createDialogLayout());

        binder.readBean(uploadOptions);
        binder.addStatusChangeListener(event -> uploadButton.setEnabled(binder.isValid()));
    }

    private Button createUploadButton() {
        uploadButton = new Button("Upload");
        uploadButton.setEnabled(binder.isValid());

        uploadButton.addClickListener(e -> {
            binder.writeBeanIfValid(uploadOptions);
            projectService.updateTargetFile(project, fileInputStream, uploadOptions.getProjectName(), uploadOptions.getBoundedContextPackages());
            UI.getCurrent().navigate(ProjectView.class, new RouteParameters(Map.of("projectName", project.getName())));
            ComponentUtil.fireEvent(UI.getCurrent(), new DiagramsOrProjectsChangedEvent(this, false));
            close();
        });

        uploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return uploadButton;
    }

    private Button createCancelButton() {
        return new Button("Cancel", e -> close());
    }

    private FormLayout createDialogLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        formLayout.addFormItem(getUpload(), "File");

        TextField projectNameTextField = new TextField();
        projectNameTextField.setWidthFull();
        binder.forField(projectNameTextField)
            .asRequired("Project name is required.")
            .bind(UploadOptions::getProjectName, UploadOptions::setProjectName);

        formLayout.addFormItem(projectNameTextField, "Project Name");

        PackageSelectChipField packageSelectChipField = new PackageSelectChipField();
        packageSelectChipField.setWidthFull();
        binder.forField(packageSelectChipField)
            .asRequired("At least one Context-Package is required.")
            .bind(UploadOptions::getBoundedContextPackages, UploadOptions::setBoundedContextPackages);

        formLayout.addFormItem(packageSelectChipField, "Context-Packages");

        return formLayout;
    }

    private Upload getUpload() {
        MemoryBuffer uploadBuffer = new MemoryBuffer();
        Upload upload = new Upload(uploadBuffer);
        upload.setWidthFull();

        upload.setMaxFileSize(500000000); // 500MB
        upload.setAcceptedFileTypes("application/java-archive");

        upload.addSucceededListener(event -> fileInputStream = uploadBuffer.getInputStream());

        return upload;
    }

    @Data
    @AllArgsConstructor
    private static class UploadOptions {
        private String projectName;
        private Set<String> boundedContextPackages;
    }
}
