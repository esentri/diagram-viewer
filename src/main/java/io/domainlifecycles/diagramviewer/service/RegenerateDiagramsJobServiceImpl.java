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

import io.domainlifecycles.diagramviewer.model.task.RegenerateDiagramsJob;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.repository.RegenerateDiagramsJobRepository;
import java.util.List;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RegenerateDiagramsJobServiceImpl implements RegenerateDiagramsJobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegenerateDiagramsJobServiceImpl.class);

    private final RegenerateDiagramsJobRepository repository;

    public RegenerateDiagramsJobServiceImpl(RegenerateDiagramsJobRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<RegenerateDiagramsJob> getAll() {
        return StreamSupport.stream(repository.findAll().spliterator(), false).toList();
    }

    @Override
    public void create(Project project) {
        project.getDiagrams().forEach(diagram -> {
            RegenerateDiagramsJob job = RegenerateDiagramsJob.builder()
                .diagram(diagram)
                .build();
            repository.save(job);
        });
    }

    @Override
    public void delete(RegenerateDiagramsJob job) {
        repository.delete(job);
    }

    @Override
    public void delete(Diagram diagram) {
        List<RegenerateDiagramsJob> foundJobs = repository.findByDiagramId(diagram.getId());
        foundJobs.forEach(this::delete);
    }
}
