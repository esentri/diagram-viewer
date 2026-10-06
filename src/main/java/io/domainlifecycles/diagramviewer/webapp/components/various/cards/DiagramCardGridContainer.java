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

import com.vaadin.flow.component.orderedlayout.FlexLayout;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.DiagramDirectoryService;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import java.util.Set;

public final class DiagramCardGridContainer extends FlexLayout {

    public DiagramCardGridContainer() {
        setSizeUndefined();
        setWidthFull();
    }

    public DiagramCardGridContainer(final DiagramDirectoryService diagramDirectoryService,
                                    final DiagramService diagramService,
                                    final Project project,
                                    final Set<DiagramDirectory> diagramDirectories,
                                    final Set<Diagram> diagrams) {
        this();
        add(new DiagramCardGrid(diagramDirectoryService, diagramService, project, diagramDirectories, diagrams));
    }

    public DiagramCardGridContainer(final DiagramDirectoryService diagramDirectoryService,
                                    final DiagramService diagramService,
                                    final Project project,
                                    final Set<Diagram> diagrams) {
        this();
        add(new DiagramCardGrid(diagramDirectoryService, diagramService, project, diagrams));
    }
}
