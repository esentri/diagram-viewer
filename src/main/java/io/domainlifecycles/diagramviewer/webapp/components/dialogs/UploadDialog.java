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
 *  Copyright 2025-2026 the original author or authors.
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

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.streams.UploadHandler;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.service.SecurityService;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.UploadFileType;
import io.domainlifecycles.diagramviewer.webapp.components.various.selects.PackageSelectChipField;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import lombok.Data;

public final class UploadDialog extends Dialog {

    private final ProjectService projectService;
    private final SecurityService securityService;
    private final Binder<UploadOptions> binder;

    private UploadOptions uploadOptions;
    private Button uploadButton;
    private PackageSelectChipField packageSelectChipField;
    private String fileName;
    private byte[] fileContents;
    private String uploadMimeType;

    public UploadDialog(ProjectService projectService, SecurityService securityService) {
        this.projectService = projectService;
        this.securityService = securityService;
        this.binder = new Binder<>();

        setHeaderTitle("Upload Project");
        setWidth("30%");
        setHeight("50%");

        getFooter().add(createUploadButton());
        getFooter().add(createCancelButton());

        addOpenedChangeListener(e -> {
            if(e.isOpened()) {
                uploadOptions = new UploadOptions();
                binder.readBean(uploadOptions);
            }
        });

        add(createDialogLayout());
        binder.addStatusChangeListener(event -> uploadButton.setEnabled(binder.isValid()));
    }

    private Button createUploadButton() {
        uploadButton = new Button("Upload");
        uploadButton.setEnabled(binder.isValid());

        uploadButton.addClickListener(e -> {
            Path pathToFile = FileIOUtils.saveTemporaryFile(fileName, fileContents);

            binder.writeBeanIfValid(uploadOptions);
            projectService.create(
                uploadOptions.getProjectName(), uploadOptions.getDomainModelPackages(),
                securityService.getCurrentlySignedInUser(), pathToFile,
                UploadFileType.findByMimeType(uploadMimeType));
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
        formLayout.addFormItem(packageSelectChipField, "Packages");
        packageSelectChipField.setEnabled(Objects.equals(uploadMimeType, UploadFileType.JAR.getMimeType()));

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
    private static class UploadOptions {
        private String projectName;
        private Set<String> domainModelPackages;
    }
}
