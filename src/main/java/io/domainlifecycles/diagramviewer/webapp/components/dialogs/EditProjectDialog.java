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
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.RouteParameters;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.service.SecurityService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.views.ProjectView;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

public class EditProjectDialog extends Dialog {

    private final Project project;
    private final ProjectService projectService;
    private final SecurityService securityService;
    private final Binder<UploadOptions> binder;

    private UploadOptions uploadOptions;
    private Button saveButton;

    public EditProjectDialog(Project project, ProjectService projectService, SecurityService securityService) {
        this.project = project;
        this.projectService = projectService;
        this.securityService = securityService;
        this.binder = new Binder<>();

        setHeaderTitle("Edit Project");
        setWidth("30%");
        setHeight("50%");

        getFooter().add(createSaveButton());
        getFooter().add(createCancelButton());
        add(createDialogLayout());

        addOpenedChangeListener(e -> {
            if(e.isOpened()) {
                this.uploadOptions = new UploadOptions(project.getName());
                binder.readBean(uploadOptions);
            }
        });

        binder.addStatusChangeListener(event -> saveButton.setEnabled(binder.isValid()));
    }

    private Button createSaveButton() {
        saveButton = new Button("Save");

        saveButton.addClickListener(e -> {
            binder.writeBeanIfValid(uploadOptions);
            projectService.rename(
                    project,
                    securityService.getCurrentlySignedInUser(),
                    uploadOptions.getProjectName());
            close();
            UI.getCurrent().navigate(ProjectView.class, new RouteParameters(Map.of(
                ProjectView.PROJECT_NAME_ROUTE_PARAMETER, project.getName())));
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
            .asRequired("Name is required.")
            .bind(UploadOptions::getProjectName, UploadOptions::setProjectName);
        formLayout.addFormItem(projectNameTextField, "Name");

        return formLayout;
    }

    @Data
    @AllArgsConstructor
    private static class UploadOptions {
        private String projectName;
    }
}
