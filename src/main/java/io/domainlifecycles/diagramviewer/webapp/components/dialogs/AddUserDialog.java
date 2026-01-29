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
 *  Copyright 2019-2025 the original author or authors.
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
import com.vaadin.flow.component.textfield.EmailField;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.webapp.events.ProjectUsersChangedEvent;

public class AddUserDialog extends Dialog {

    private final Project project;
    private final ProjectService projectService;

    private String emailAddressValue;

    public AddUserDialog(Project project, ProjectService projectService) {
        this.project = project;
        this.projectService = projectService;

        setHeaderTitle("Add User");

        setWidth("30%");
        setHeight("40%");

        add(createDialogLayout());

        getFooter().add(createAddButton());
        getFooter().add(createCancelButton());
    }


    private Button createAddButton() {
        Button saveButton = new Button("Add");

        saveButton.addClickListener(e -> {
            projectService.assignUser(project, emailAddressValue);
            ComponentUtil.fireEvent(UI.getCurrent(), new ProjectUsersChangedEvent(this, false));
            close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return saveButton;
    }

    private Button createCancelButton() {
        return new Button("Cancel", e -> close());
    }

    private FormLayout createDialogLayout() {
        FormLayout formLayout = new FormLayout();

        EmailField emailField = new EmailField();
        emailField.getElement().setAttribute("name", "email");
        emailField.setErrorMessage("Not a valid email address");
        emailField.setClearButtonVisible(true);
        emailField.addValueChangeListener(valueChanged -> emailAddressValue = valueChanged.getValue());

        formLayout.addFormItem(emailField, "Email address");

        return formLayout;
    }
}
