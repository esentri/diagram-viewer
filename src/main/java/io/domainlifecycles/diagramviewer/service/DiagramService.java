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
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.staticanalysis.DomainCalls;
import java.util.Set;
import java.util.UUID;

public interface DiagramService {

    Set<Diagram> findAll(UUID projectId);

    Diagram updateModel(Diagram diagram);

    Diagram updateModelAndImage(Diagram diagram);

    Diagram rename(Diagram diagram, String fileName);

    Diagram create(Project project, String fileName, DomainModelVisibility visibility, DiagramStylingConfiguration diagramStylingConfiguration);

    /**
     * Saves the diagram's model right away and renders its image in the background, so that the user interface is
     * not blocked while large diagrams are generated and converted by Kroki. A rendering that is superseded by a
     * newer one of the same diagram is dropped - before it starts, or at the latest before its image is saved.
     *
     * @param diagram the diagram to save and render
     * @return the saved diagram and the pending rendering of its image
     */
    DiagramRendering updateModelAndImageAsync(Diagram diagram);

    /**
     * Creates a diagram like {@link #create(Project, String, DomainModelVisibility, DiagramStylingConfiguration)},
     * but renders its image in the background, see {@link #updateModelAndImageAsync(Diagram)}.
     */
    DiagramRendering createAsync(Project project, String fileName, DomainModelVisibility visibility, DiagramStylingConfiguration diagramStylingConfiguration);

    /**
     * Like {@link #createAsync(Project, String, DomainModelVisibility, DiagramStylingConfiguration)}, creating the
     * diagram right in the given directory - with a single save, which matters when many diagrams are created at once.
     *
     * @param directory the directory of the new diagram, {@code null} for none
     */
    DiagramRendering createAsync(Project project, DiagramDirectory directory, String fileName, DomainModelVisibility visibility,
                                 DiagramStylingConfiguration diagramStylingConfiguration);

    /**
     * Renders the given diagram and saves it to the filesystem.
     * <p>
     * All model data is passed in explicitly, so this can be called without an HTTP request or session
     * bound - e.g. from the scheduled diagram regeneration.
     *
     * @param domainMirror the domain mirror of the diagram's project
     * @param domainCalls  the static analysis result of the diagram's project, {@code null} if none was
     *                     uploaded (flow filters are then ignored)
     * @param diagram      the diagram to render
     */
    void createAndSaveDiagramToFilesystem(DomainMirror domainMirror, DomainCalls domainCalls, Diagram diagram);

    void deleteFilesFromFilesystem(String projectId);
}