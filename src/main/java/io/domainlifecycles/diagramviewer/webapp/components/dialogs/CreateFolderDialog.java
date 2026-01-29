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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.DiagramDirectoryService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CreateFolderDialog extends Dialog {

    private final Project project;
    private final Diagram dragDiagram;
    private final Diagram dropDiagram;
    private final DiagramDirectoryService diagramDirectoryService;
    private final Binder<CreateFolderOptions> binder;

    private CreateFolderOptions createFolderOptions;
    private Button createButton;

    public CreateFolderDialog(Project project, Diagram dragDiagram, Diagram dropDiagram, DiagramDirectoryService diagramDirectoryService) {
        this.project = project;
        this.dragDiagram = dragDiagram;
        this.dropDiagram = dropDiagram;
        this.diagramDirectoryService = diagramDirectoryService;
        this.binder = new Binder<>();

        this.createFolderOptions = new CreateFolderOptions();

        setHeaderTitle("Create Folder");

        getFooter().add(createCreateButton());
        getFooter().add(createCancelButton());
        add(createDialogLayout());

        addOpenedChangeListener(e -> {
            if(e.isOpened()) {
                this.createFolderOptions = new CreateFolderOptions();
                binder.readBean(createFolderOptions);
            }
        });

        binder.addStatusChangeListener(event -> createButton.setEnabled(binder.isValid()));
    }

    private Button createCreateButton() {
        createButton = new Button("Create");

        createButton.addClickListener(e -> {
            binder.writeBeanIfValid(createFolderOptions);
            diagramDirectoryService.create(createFolderOptions.getFolderName(), project, Set.of(dragDiagram, dropDiagram));
            close();
            ComponentUtil.fireEvent(UI.getCurrent(), new DiagramsOrProjectsChangedEvent(this, false));
        });

        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return createButton;
    }

    private Button createCancelButton() {
        return new Button("Cancel", e -> close());
    }

    private FormLayout createDialogLayout() {
        FormLayout formLayout = new FormLayout();

        TextField folderNameTextField = new TextField();
        binder.forField(folderNameTextField)
            .asRequired("Name is required.")
            .bind(CreateFolderOptions::getFolderName, CreateFolderOptions::setFolderName);
        formLayout.addFormItem(folderNameTextField, "Name");

        return formLayout;
    }

    @Data
    @NoArgsConstructor
    private static class CreateFolderOptions {
        private String folderName;
    }
}
