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
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import java.util.Set;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public interface DiagramDirectoryService {

    DiagramDirectory getById(UUID id);

    /**
     * Returns the directory with the given name nested directly in the given parent, creating it if there is none.
     *
     * @param project the project of the directory
     * @param parent  the parent directory, {@code null} for a directory directly below the project
     * @param name    the name of the directory
     * @return the found or created directory
     */
    @Transactional
    DiagramDirectory findOrCreate(Project project, DiagramDirectory parent, String name);

    @Transactional
    void create(String name, Project project, Set<Diagram> diagrams);

    @Transactional
    void add(DiagramDirectory diagramDirectory, Diagram diagram);

    void update(DiagramDirectory diagramDirectory, String name);
}
