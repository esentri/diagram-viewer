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
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.service.DiagramDirectoryService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramsOrProjectsChangedEvent;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramDirectoryView;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

public final class RenameDiagramDirectoryDialog extends Dialog {

    private final DiagramDirectoryService diagramDirectoryService;
    private final DiagramDirectory diagramDirectory;
    private final Binder<RenameDiagramDirectoryOptions> binder;

    private RenameDiagramDirectoryOptions renameDiagramDirectoryOptions;
    private Button saveButton;

    public RenameDiagramDirectoryDialog(DiagramDirectoryService diagramDirectoryService, DiagramDirectory diagramDirectory) {
        this.diagramDirectoryService = diagramDirectoryService;
        this.diagramDirectory = diagramDirectory;
        this.binder = new Binder<>();

        setHeaderTitle("Rename Directory");

        getFooter().add(createSaveButton());
        getFooter().add(createCancelButton());
        add(createDialogLayout());

        addOpenedChangeListener(e -> {
            if(e.isOpened()) {
                this.renameDiagramDirectoryOptions = new RenameDiagramDirectoryOptions(diagramDirectory.getName());
                binder.readBean(renameDiagramDirectoryOptions);
            }
        });

        binder.addStatusChangeListener(event -> saveButton.setEnabled(binder.isValid()));
    }

    private Button createSaveButton() {
        saveButton = new Button("Save");

        saveButton.addClickListener(e -> {
            binder.writeBeanIfValid(renameDiagramDirectoryOptions);
            diagramDirectoryService.update(diagramDirectory, renameDiagramDirectoryOptions.getName());
            close();
            UI.getCurrent().navigate(DiagramDirectoryView.class, new RouteParameters(
                    Map.of(DiagramDirectoryView.DIAGRAM_DIRECTORY_ID_ROUTE_PARAMETER, diagramDirectory.getId().toString())));
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

        TextField diagramDirectoryNameTextField = new TextField();
        binder.forField(diagramDirectoryNameTextField)
            .asRequired("Name is required.")
            .bind(RenameDiagramDirectoryOptions::getName, RenameDiagramDirectoryOptions::setName);
        formLayout.addFormItem(diagramDirectoryNameTextField, "Name");

        return formLayout;
    }

    @Data
    @AllArgsConstructor
    private static class RenameDiagramDirectoryOptions {
        private String name;
    }
}
