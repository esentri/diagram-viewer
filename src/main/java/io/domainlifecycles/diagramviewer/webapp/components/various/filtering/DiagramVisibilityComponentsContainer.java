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

package io.domainlifecycles.diagramviewer.webapp.components.various.filtering;

import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;

public class DiagramVisibilityComponentsContainer extends VerticalLayout {

    private DiagramFilterComponent diagramFilterComponent;
    private DiagramFlowFilterComponent diagramFlowFilterComponent;
    private DiagramVisibilityComponent diagramVisibilityComponent;

    public DiagramVisibilityComponentsContainer(
            SessionStorage sessionStorage,
            DiagramService diagramService
    ) {

        setWidthFull();
        this.diagramFilterComponent = new DiagramFilterComponent(sessionStorage, diagramService);
        this.diagramFlowFilterComponent = new DiagramFlowFilterComponent(sessionStorage, diagramService);
        this.diagramVisibilityComponent = new DiagramVisibilityComponent(sessionStorage, diagramService);
        add(this.diagramFilterComponent,
            this.diagramFlowFilterComponent,
            new Hr(),
            this.diagramVisibilityComponent);
    }

    public void setDiagram(Diagram diagram) {
        this.diagramVisibilityComponent.setDiagram(diagram);
        this.diagramFilterComponent.setDiagram(diagram);
        this.diagramFlowFilterComponent.setDiagram(diagram);
    }
}
