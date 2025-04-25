package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.service.SecurityService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import java.io.InputStream;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadDialog extends Dialog {

    private final static Logger log = LoggerFactory.getLogger(UploadDialog.class);

    private final ProjectService projectService;
    private final SecurityService securityService;
    private final Binder<UploadOptions> binder;

    private Button uploadButton;
    private TextField projectNameTextField;
    private TextField boundedContextPackagesTextField;
    private InputStream fileInputStream;

    public UploadDialog(ProjectService projectService, SecurityService securityService) {
        this.projectService = projectService;
        this.securityService = securityService;
        this.binder = new Binder<>();

        setHeaderTitle("Upload Project");
        setWidth("30%");
        setHeight("50%");

        getFooter().add(createUploadButton());
        getFooter().add(createCancelButton());
        add(createDialogLayout());
    }

    private Button createUploadButton() {
        uploadButton = new Button("Upload");
        uploadButton.setEnabled(binder.isValid());

        uploadButton.addClickListener(e -> {
            projectService.save(securityService.getCurrentlySignedInUser(), fileInputStream,
                projectNameTextField.getValue(), boundedContextPackagesTextField.getValue());
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

        projectNameTextField = new TextField();
        projectNameTextField.setWidthFull();
        binder.forField(projectNameTextField)
            .asRequired("Project name may not be empty")
            .bind(UploadOptions::getProjectName, UploadOptions::setProjectName);
        binder.addStatusChangeListener(event -> uploadButton.setEnabled(binder.isValid()));

        formLayout.addFormItem(projectNameTextField, "Project Name");

        boundedContextPackagesTextField = new TextField();
        boundedContextPackagesTextField.setWidthFull();
        binder.forField(boundedContextPackagesTextField)
            .asRequired("Bounded Contexts may not be empty")
            .bind(UploadOptions::getBoundedContextPackages, UploadOptions::setBoundedContextPackages);
        binder.addStatusChangeListener(event -> uploadButton.setEnabled(binder.isValid()));

        formLayout.addFormItem(boundedContextPackagesTextField, "Bounded Context Packages (comma separated)");

        return formLayout;
    }

    private Upload getUpload() {
        MultiFileMemoryBuffer uploadBuffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(uploadBuffer);
        upload.setWidthFull();

        upload.setMaxFileSize(500000000); // 500MB
        upload.setAcceptedFileTypes("application/java-archive");

        upload.addSucceededListener(event -> fileInputStream = uploadBuffer.getInputStream(event.getFileName()));
        return upload;
    }

    @Data
    private static class UploadOptions {
        private String projectName;
        private String boundedContextPackages;
    }
}
