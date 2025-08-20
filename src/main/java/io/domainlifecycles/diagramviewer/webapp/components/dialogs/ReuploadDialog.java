package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.streams.UploadHandler;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.service.SecurityService;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.UploadFileType;
import io.domainlifecycles.diagramviewer.webapp.components.various.selects.PackageSelectChipField;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.views.ProjectView;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;

public class ReuploadDialog extends Dialog {

    private final ProjectService projectService;
    private final SecurityService securityService;
    private final Binder<UploadOptions> binder;
    private final Project project;

    private UploadOptions uploadOptions;
    private Button uploadButton;
    private PackageSelectChipField packageSelectChipField;
    private String fileName;
    private byte[] fileContents;
    private String uploadMimeType;

    public ReuploadDialog(Project project, ProjectService projectService, SecurityService securityService) {
        this.project = project;
        this.projectService = projectService;
        this.securityService = securityService;
        this.binder = new Binder<>();

        setHeaderTitle("Reupload Project");
        setWidth("30%");
        setHeight("50%");

        getFooter().add(createUploadButton());
        getFooter().add(createCancelButton());
        add(createDialogLayout());

        addOpenedChangeListener(e -> {
            if(e.isOpened()) {
                uploadOptions = new UploadOptions(project.getDomainModelPackages());
                binder.readBean(uploadOptions);
            }
        });

        binder.addStatusChangeListener(event -> uploadButton.setEnabled(binder.isValid()));
    }

    private Button createUploadButton() {
        uploadButton = new Button("Upload");
        uploadButton.setEnabled(binder.isValid());

        uploadButton.addClickListener(e -> {
            Path pathToFile;
            try {
                pathToFile = Files.createTempFile(
                    fileName.substring(0, fileName.lastIndexOf('.')),
                    fileName.substring(fileName.lastIndexOf('.')));
                Files.write(pathToFile, fileContents);
            } catch (IOException ex) {
                throw DiagramViewerException.fail("Could not save temporary .jar file.", e);
            }

            binder.writeBeanIfValid(uploadOptions);
            projectService.updateDomainMirror(
                    project,
                    uploadOptions.getDomainModelPackages(),
                    securityService.getCurrentlySignedInUser(),
                    pathToFile,
                    UploadFileType.findByMimeType(uploadMimeType)
            );
            UI.getCurrent().navigate(ProjectView.class, new RouteParameters(Map.of(ProjectView.PROJECT_NAME_ROUTE_PARAMETER, project.getName())));
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

        packageSelectChipField = new PackageSelectChipField();
        packageSelectChipField.setWidthFull();
        binder.forField(packageSelectChipField)
            .withValidator(packages -> {
                if (Objects.equals(uploadMimeType, UploadFileType.JAR.getMimeType())) {
                    return packages != null && !packages.isEmpty();
                }
                return true;
            }, "At least one package is required.")
            .bind(UploadOptions::getDomainModelPackages, UploadOptions::setDomainModelPackages);
        packageSelectChipField.setEnabled(Objects.equals(uploadMimeType, UploadFileType.JAR.getMimeType()));

        formLayout.addFormItem(packageSelectChipField, "Packages");

        return formLayout;
    }

    private Upload getUpload() {
        UploadHandler inMemoryUploadHandler = UploadHandler.inMemory(
            (uploadMetadata, bytes) -> {
                fileName = uploadMetadata.fileName();
                fileContents = bytes;
                uploadMimeType = uploadMetadata.contentType();
                packageSelectChipField.setEnabled(Objects.equals(uploadMimeType, UploadFileType.JAR.getMimeType()));
            });

        Upload upload = new Upload(inMemoryUploadHandler);
        upload.setWidthFull();

        upload.setMaxFileSize(500000000); // 500MB
        upload.setAcceptedFileTypes("application/java-archive", "application/json", ".jar", ".json");

        return upload;
    }

    @Data
    @AllArgsConstructor
    private static class UploadOptions {
        private Set<String> domainModelPackages;
    }
}
