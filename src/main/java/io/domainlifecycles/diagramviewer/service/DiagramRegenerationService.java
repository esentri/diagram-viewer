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

package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Regenerates diagrams outside of any user interaction, e.g. from the scheduled regeneration after a
 * project's domain model was uploaded again.
 * <p>
 * It runs without an HTTP request or session bound, so it must not use the session scoped
 * {@code SessionStorage}. It takes the project's model data from the {@link ProjectModelCache} shared with the
 * sessions instead - loaded once per project, and the static analysis result only if a diagram needs it.
 */
@Service
public class DiagramRegenerationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiagramRegenerationService.class);

    private final ProjectModelCache projectModelCache;

    private final DiagramService diagramService;

    public DiagramRegenerationService(
            ProjectModelCache projectModelCache,
            DiagramService diagramService
    ) {
        this.projectModelCache = projectModelCache;
        this.diagramService = diagramService;
    }

    /**
     * Returns the current model data of a project, shared with the sessions via the {@link ProjectModelCache}.
     *
     * @param projectId the project to load
     * @return the project's model data
     */
    public ProjectModel loadProjectModel(UUID projectId) {
        return projectModelCache.get(projectId);
    }

    /**
     * Regenerates a diagram from already loaded model data of its project.
     *
     * @param diagram      the diagram to regenerate
     * @param projectModel the model data of the diagram's project, see {@link #loadProjectModel(UUID)}
     */
    public void regenerate(Diagram diagram, ProjectModel projectModel) {
        LOGGER.info(String.format("Regenerating diagram '%s'.", diagram.getName()));
        var domainCalls = diagram.getDomainModelVisibility() != null && diagram.getDomainModelVisibility().hasFlowSettings()
            ? projectModel.domainCalls().orElse(null)
            : null;
        diagramService.createAndSaveDiagramToFilesystem(projectModel.domainMirror(), domainCalls, diagram);
    }

    /**
     * Regenerates a single diagram, loading its project's model data first. To regenerate several
     * diagrams of the same project, load the model once and use {@link #regenerate(Diagram, ProjectModel)}.
     *
     * @param diagram the diagram to regenerate
     */
    public void regenerate(Diagram diagram) {
        regenerate(diagram, loadProjectModel(diagram.getProject().getId()));
    }
}
