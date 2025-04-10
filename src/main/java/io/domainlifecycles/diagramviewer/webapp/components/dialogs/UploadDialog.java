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
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.session.SessionStorage;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadDialog extends Dialog {

    private final static Logger log = LoggerFactory.getLogger(UploadDialog.class);

    private final ProjectService projectService;
    private final SessionStorage sessionStorage;
    private String boundedContextPackages;
    private InputStream fileInputStream;
    private String fileName;

    public UploadDialog(ProjectService projectService, SessionStorage sessionStorage) {
        this.projectService = projectService;
        this.sessionStorage = sessionStorage;
        add(createDialogLayout());
        getFooter().add(createUploadButton());
        getFooter().add(createCancelButton());
    }

    private Button createUploadButton() {
        final String targetsLocation = sessionStorage.getTargetsLocation();
        Button uploadButton = new Button("Upload");

        uploadButton.addClickListener(e -> {
            if(targetsLocation == null || targetsLocation.isBlank()) {
                throw DiagramViewerException.fail("No upload location for targets specified.");
            }

            projectService.save(targetsLocation, fileInputStream, fileName, boundedContextPackages);
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

        TextField boundedContextPackagesTextField = new TextField();
        boundedContextPackagesTextField.addValueChangeListener(e -> boundedContextPackages = boundedContextPackagesTextField.getValue());
        formLayout.addFormItem(boundedContextPackagesTextField, "Bounded Context Packages (comma separated)");

        return formLayout;
    }

    private Upload getUpload() {
        MultiFileMemoryBuffer uploadBuffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(uploadBuffer);

        upload.setMaxFileSize(100000000); // 100MB
        //upload.setAcceptedFileTypes("jar");

        upload.addSucceededListener(event -> {
            fileName = event.getFileName();
            fileInputStream = uploadBuffer.getInputStream(fileName);
        });
        return upload;
    }
}
