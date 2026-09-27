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

package io.domainlifecycles.diagramviewer.webapp.components.various.cards;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.RouteParameters;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.DiagramDirectoryService;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.DiagramServiceImpl;
import io.domainlifecycles.diagramviewer.util.DiagramFileUtils;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramDirectoryView;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramView;
import io.domainlifecycles.diagramviewer.webapp.views.ProjectView;
import java.util.Map;

public class CardLinkWrapper extends Div {

    private CardLinkWrapper() {
        getStyle().set("cursor", "pointer");
        getStyle().setMarginBottom("calc(var(--vaadin-form-layout-column-spacing))");
    }

    public CardLinkWrapper(DiagramDirectoryService diagramDirectoryService, DiagramService diagramService, Project project,
                           Diagram diagram) {
        this();
        addClickListener(
            event -> UI.getCurrent().navigate(DiagramView.class, new RouteParameters(
                Map.of(ProjectView.PROJECT_NAME_ROUTE_PARAMETER, project.getName(),
                    DiagramView.DIAGRAM_NAME_ROUTE_PARAMETER, diagram.getName()))));

        add(createAndGetDiagramCard(diagramDirectoryService, diagramService, project, diagram));
    }

    public CardLinkWrapper(DiagramDirectoryService diagramDirectoryService, DiagramDirectory diagramDirectory) {
        this();
        addClickListener(
            event -> UI.getCurrent().navigate(DiagramDirectoryView.class, new RouteParameters(
                Map.of(DiagramDirectoryView.DIAGRAM_DIRECTORY_ID_ROUTE_PARAMETER, diagramDirectory.getId().toString()))));

        add(createAndGetDiagramCard(diagramDirectoryService, diagramDirectory));
    }

    private DiagramCard createAndGetDiagramCard(DiagramDirectoryService diagramDirectoryService, DiagramService diagramService,
                                                Project project, Diagram diagram) {
        return new DiagramCard(diagramDirectoryService, diagram, diagramService.imageSize(diagram),
            diagramService.previewLimitBytes(),
            DiagramFileUtils.assembleDiagramUrl(
                diagram.getChangedAt(), diagram.getDiagramStylingConfiguration().getChangedAt(),
                project.getId().toString(), diagram.getName() + DiagramServiceImpl.SVG_FILE_SUFFIX));
    }

    private DiagramCard createAndGetDiagramCard(DiagramDirectoryService diagramDirectoryService, DiagramDirectory diagramDirectory) {
        return new DiagramCard(diagramDirectoryService, diagramDirectory);
    }
}
