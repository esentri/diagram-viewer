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
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.staticanalysis.DomainCalls;
import java.util.UUID;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Regenerates diagrams outside of any user interaction, e.g. from the scheduled regeneration after a
 * project's domain model was uploaded again.
 * <p>
 * It runs without an HTTP request or session bound, so it must not use the session scoped
 * {@code SessionStorage}: the project's domain mirror and static analysis result are loaded from the
 * database instead - once per project via {@link #loadProjectModel(UUID)}, then shared by all of that
 * project's diagrams.
 */
@Service
public class DiagramRegenerationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiagramRegenerationService.class);

    private final ProjectDomainMirrorService projectDomainMirrorService;

    private final DiagramService diagramService;

    public DiagramRegenerationService(
            ProjectDomainMirrorService projectDomainMirrorService,
            DiagramService diagramService
    ) {
        this.projectDomainMirrorService = projectDomainMirrorService;
        this.diagramService = diagramService;
    }

    /**
     * The model data a project's diagrams are rendered from. The domain mirror is loaded right away, the
     * static analysis result only on first access - it is only needed for diagrams restricted to a flow,
     * and then loaded at most once per project.
     */
    public static final class ProjectModel {

        private final DomainMirror domainMirror;
        private final Supplier<DomainCalls> domainCallsLoader;
        private boolean domainCallsLoaded;
        private DomainCalls domainCalls;

        public ProjectModel(DomainMirror domainMirror, Supplier<DomainCalls> domainCallsLoader) {
            this.domainMirror = domainMirror;
            this.domainCallsLoader = domainCallsLoader;
        }

        public DomainMirror domainMirror() {
            return domainMirror;
        }

        /**
         * @return the project's static analysis result, {@code null} if none was uploaded
         */
        public DomainCalls domainCalls() {
            if (!domainCallsLoaded) {
                domainCalls = domainCallsLoader.get();
                domainCallsLoaded = true;
            }
            return domainCalls;
        }
    }

    /**
     * Loads the model data of a project from the database: the domain mirror right away, the static
     * analysis result lazily, see {@link ProjectModel}.
     *
     * @param projectId the project to load
     * @return the project's model data
     */
    public ProjectModel loadProjectModel(UUID projectId) {
        DomainMirror domainMirror = projectDomainMirrorService.getDomainMirror(projectId);
        return new ProjectModel(domainMirror,
            () -> projectDomainMirrorService.loadDomainCalls(projectId, domainMirror).orElse(null));
    }

    /**
     * Regenerates a diagram from already loaded model data of its project.
     *
     * @param diagram      the diagram to regenerate
     * @param projectModel the model data of the diagram's project, see {@link #loadProjectModel(UUID)}
     */
    public void regenerate(Diagram diagram, ProjectModel projectModel) {
        LOGGER.info(String.format("Regenerating diagram '%s'.", diagram.getName()));
        DomainCalls domainCalls = diagram.getDomainModelVisibility() != null && diagram.getDomainModelVisibility().hasFlowSettings()
            ? projectModel.domainCalls()
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
