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

package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.repository.DiagramDirectoryRepository;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class DiagramDirectoryServiceImpl implements DiagramDirectoryService {

    private final DiagramService diagramService;
    private final DiagramDirectoryRepository repository;

    public DiagramDirectoryServiceImpl(DiagramService diagramService, DiagramDirectoryRepository repository) {
        this.diagramService = diagramService;
        this.repository = repository;
    }

    @Override
    public DiagramDirectory getByName(String name) {
        return repository.findByName(name).orElseThrow(() ->
            DiagramViewerException.fail(String.format("No Diagram Directory found with name '%s'.", name)));
    }

    @Override
    public void create(String name, Project project, Set<Diagram> diagrams) {
        DiagramDirectory diagramDirectory = DiagramDirectory.builder()
            .name(name)
            .diagrams(new HashSet<>(diagrams))
            .project(project)
            .build();

        repository.save(diagramDirectory);

        project.addDiagramDirectory(diagramDirectory);
        diagrams.forEach(diagram -> {
            diagram.setDiagramDirectory(diagramDirectory);
            diagramService.updateModel(diagram);
        });
    }

    @Override
    public void add(DiagramDirectory diagramDirectory, Diagram diagram) {
        diagramDirectory.addDiagram(diagram);
        repository.save(diagramDirectory);
        diagramService.updateModel(diagram);
    }

    @Override
    public void update(DiagramDirectory diagramDirectory, String name) {
        diagramDirectory.setName(name);
        repository.save(diagramDirectory);
    }
}
