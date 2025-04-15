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
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.session.SessionStorage;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import java.io.InputStream;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadDialog extends Dialog {

    private final static Logger log = LoggerFactory.getLogger(UploadDialog.class);

    private final ProjectService projectService;
    private final SessionStorage sessionStorage;
    private final Binder<UploadOptions> binder;

    private Button uploadButton;
    private TextField boundedContextPackagesTextField;
    private InputStream fileInputStream;
    private String fileName;

    public UploadDialog(ProjectService projectService, SessionStorage sessionStorage) {
        this.projectService = projectService;
        this.sessionStorage = sessionStorage;
        this.binder = new Binder<>();

        add(createDialogLayout());
        getFooter().add(createUploadButton());
        getFooter().add(createCancelButton());
    }

    private Button createUploadButton() {
        final String targetsLocation = sessionStorage.getTargetsLocation();
        uploadButton = new Button("Upload");
        uploadButton.setEnabled(binder.isValid());

        uploadButton.addClickListener(e -> {
            if(targetsLocation == null || targetsLocation.isBlank()) {
                throw DiagramViewerException.fail("No upload location for targets specified.");
            }

            Project persistedProject = projectService.save(sessionStorage.getAuthenticatedUser(), targetsLocation, fileInputStream,
                fileName, boundedContextPackagesTextField.getValue());
            sessionStorage.add(persistedProject);
            ComponentUtil.fireEvent(UI.getCurrent(), new DiagramsOrProjectsChangedEvent(this, false));

            log.info(String.format("Successfully uploaded file '%s' to '%s'.", fileName, targetsLocation));
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

        formLayout.addFormItem(getUpload(), "File");

        boundedContextPackagesTextField = new TextField();
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

        upload.setMaxFileSize(500000000); // 500MB
        upload.setAcceptedFileTypes("application/java-archive");

        upload.addSucceededListener(event -> {
            fileName = event.getFileName();
            fileInputStream = uploadBuffer.getInputStream(fileName);
        });
        return upload;
    }

    @Data
    private static class UploadOptions {
        private String boundedContextPackages;
    }
}
