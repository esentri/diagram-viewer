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

import com.vaadin.flow.component.formlayout.FormLayout;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.DiagramDirectoryService;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import java.util.Comparator;
import java.util.Set;

public class DiagramCardGrid extends FormLayout {

    private DiagramCardGrid() {
        setSizeFull();
        getStyle().setMarginTop("2rem");
        setResponsiveSteps(
            new ResponsiveStep("300px", 2),
            new ResponsiveStep("600px", 3),
            new ResponsiveStep("900px", 4),
            new ResponsiveStep("1200px", 5)
        );
    }

    public DiagramCardGrid(
        DiagramDirectoryService diagramDirectoryService,
        DiagramService diagramService,
        Project project,
        Set<DiagramDirectory> diagramDirectories,
        Set<Diagram> diagrams) {

        this();
        buildGrid(diagramDirectoryService, diagramService, project, diagramDirectories, diagrams);
    }

    public DiagramCardGrid(
        DiagramDirectoryService diagramDirectoryService,
        DiagramService diagramService,
        Project project,
        Set<Diagram> diagrams) {

        this();
        buildGrid(diagramDirectoryService, diagramService, project, diagrams);
    }

    private void buildGrid(DiagramDirectoryService diagramDirectoryService, DiagramService diagramService, Project project, Set<DiagramDirectory> diagramDirectories, Set<Diagram> diagrams) {

        diagramDirectories
            .stream()
            .sorted(Comparator.comparing(DiagramDirectory::getCreatedAt))
            .forEach(diagramDirectory -> {
                CardLinkWrapper cardLinkWrapper = new CardLinkWrapper(diagramDirectoryService, diagramDirectory);
                add(cardLinkWrapper);
            });

        diagrams
            .stream()
            .sorted(Comparator.comparing(Diagram::getCreatedAt))
            .forEach(diagram -> {
                CardLinkWrapper cardLinkWrapper = new CardLinkWrapper(diagramDirectoryService, diagramService, project, diagram);
                add(cardLinkWrapper);
            });
    }

    private void buildGrid(DiagramDirectoryService diagramDirectoryService, DiagramService diagramService, Project project, Set<Diagram> diagrams) {
        diagrams
            .stream()
            .sorted(Comparator.comparing(Diagram::getCreatedAt))
            .forEach(diagram -> {
                CardLinkWrapper cardLinkWrapper = new CardLinkWrapper(diagramDirectoryService, diagramService, project, diagram);
                add(cardLinkWrapper);
            });
    }
}
