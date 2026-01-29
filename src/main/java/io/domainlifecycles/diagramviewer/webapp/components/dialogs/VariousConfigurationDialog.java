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
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.binder.Binder;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.Acycler;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.Direction;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.Font;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.Ranker;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramStylingChangedEvent;

public class VariousConfigurationDialog extends Dialog {

    private Diagram diagram;

    private final DiagramService diagramService;
    private final Binder<DiagramStylingConfiguration> diagramConfigurationBinder;

    public void setDiagram(Diagram diagram){
        this.diagram = diagram;
        diagramConfigurationBinder.readBean(diagram.getDiagramStylingConfiguration());
    }

    public VariousConfigurationDialog(DiagramService diagramService) {
        this.diagramService = diagramService;
        this.diagramConfigurationBinder = new Binder<>(DiagramStylingConfiguration.class);

        setHeaderTitle("Configuration | Various");
        setWidth("40%");
        setHeight("60%");

        add(createDialogLayout());

        addOpenedChangeListener(e -> {
            if(e.isOpened() && diagram != null) {
                diagramConfigurationBinder.readBean(diagram.getDiagramStylingConfiguration());
            }
        });

        getFooter().add(createSaveButton());
        getFooter().add(createCancelButton());
    }

    private Button createSaveButton() {
        Button saveButton = new Button("Save");

        saveButton.addClickListener(e -> {
            diagramConfigurationBinder.writeBeanIfValid(diagram.getDiagramStylingConfiguration());
            diagram = diagramService.updateModelAndImage(diagram);
            ComponentUtil.fireEvent(UI.getCurrent(), new DiagramStylingChangedEvent(this, false));
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

        Select<Font> fontSelect = new Select<>();
        fontSelect.setItems(Font.values());
        fontSelect.setItemLabelGenerator(Font::getDisplayValue);
        formLayout.addFormItem(fontSelect,"Font");
        diagramConfigurationBinder.forField(fontSelect).bind(DiagramStylingConfiguration::getFont, DiagramStylingConfiguration::setFont);

        Select<Direction> directionSelect = new Select<>();
        directionSelect.setItems(Direction.values());
        directionSelect.setItemLabelGenerator(Direction::getDisplayValue);
        formLayout.addFormItem(directionSelect,"Direction");
        diagramConfigurationBinder.forField(directionSelect).bind(DiagramStylingConfiguration::getDirection, DiagramStylingConfiguration::setDirection);

        Select<Ranker> rankerSelect = new Select<>();
        rankerSelect.setItems(Ranker.values());
        rankerSelect.setItemLabelGenerator(Ranker::getDisplayValue);
        formLayout.addFormItem(rankerSelect,"Ranker");
        diagramConfigurationBinder.forField(rankerSelect).bind(DiagramStylingConfiguration::getRanker, DiagramStylingConfiguration::setRanker);

        Select<Acycler> acyclerSelect = new Select<>();
        acyclerSelect.setItems(Acycler.values());
        acyclerSelect.setItemLabelGenerator(Acycler::getDisplayValue);
        formLayout.addFormItem(acyclerSelect,"Acycler");
        diagramConfigurationBinder.forField(acyclerSelect).bind(DiagramStylingConfiguration::getAcycler, DiagramStylingConfiguration::setAcycler);

        return formLayout;
    }

}
