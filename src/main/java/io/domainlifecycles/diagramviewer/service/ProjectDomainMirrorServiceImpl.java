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

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.ProjectDomainMirror;
import io.domainlifecycles.diagramviewer.repository.ProjectDomainMirrorRepository;
import io.domainlifecycles.diagramviewer.util.DomainModelUtils;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.UploadFileType;
import io.domainlifecycles.mirror.api.AggregateRootMirror;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.mirror.serialize.DomainSerializer;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ProjectDomainMirrorServiceImpl implements ProjectDomainMirrorService {

    private final RegenerateDiagramsJobService regenerateDiagramsJobService;
    private final ProjectDomainMirrorRepository repository;
    private final DomainSerializer serializer;

    public ProjectDomainMirrorServiceImpl(
            RegenerateDiagramsJobService regenerateDiagramsJobService,
            ProjectDomainMirrorRepository repository,
            DomainSerializer serializer) {
        this.regenerateDiagramsJobService = regenerateDiagramsJobService;
        this.repository = repository;
        this.serializer = serializer;
    }

    @Override
    public ProjectDomainMirror getByProjectId(UUID projectId) {
        return repository.findByProjectId(projectId)
            .orElseThrow(() -> DiagramViewerException.fail(String.format("No DomainTypeMirror found for project with id '%s'.", projectId)));
    }

    @Override
    public List<DomainTypeMirror> getAllDomainTypeMirrorsWithoutEnumsAndIds(UUID projectId) {
        return repository.findProjectDomainTypesWithoutEnumsAndIds(projectId)
                .stream()
                .map(m -> (DomainTypeMirror)serializer.deserializeTypeMirror(m))
                .toList();
    }

    @Override
    public List<AggregateRootMirror> getAllAggregateRootMirrors(UUID projectId) {
        return repository.findProjectAggregateTypes(projectId)
                .stream()
                .map(m -> (AggregateRootMirror)serializer.deserializeTypeMirror(m))
                .toList();
    }

    @Override
    public ProjectDomainMirror createOrUpdate(Project project, Set<String> domainModelPackages, Path pathToFile, UploadFileType uploadFileType) {
        DomainMirror domainMirror = generateDomainMirror(pathToFile, domainModelPackages, uploadFileType);
        return createOrUpdate(project, domainMirror, null);
    }

    @Override
    public ProjectDomainMirror createOrUpdate(Project project, DomainMirror domainMirror, String domainCallsJson) {
        Optional<ProjectDomainMirror> foundProjectDomainMirror = repository.findByProjectId(project.getId());
        ProjectDomainMirror projectDomainMirror;

        if(foundProjectDomainMirror.isPresent()) {
            projectDomainMirror = foundProjectDomainMirror.get();
            projectDomainMirror.setDomainMirror(domainMirror);
            projectDomainMirror.setDomainCalls(domainCallsJson);
            regenerateDiagramsJobService.create(project);
        }
        else {
            projectDomainMirror = ProjectDomainMirror.builder()
                .projectId(project.getId())
                .domainMirror(domainMirror)
                .domainCalls(domainCallsJson)
                .build();
        }

        return repository.save(projectDomainMirror);
    }

    @Override
    public void delete(UUID projectId) {
        ProjectDomainMirror projectDomainMirror = getByProjectId(projectId);
        repository.delete(projectDomainMirror);
    }

    private DomainMirror generateDomainMirror(Path pathToJarFile, Set<String> domainModelPackages, UploadFileType uploadFileType) {
        return DomainModelUtils.initializeDomainMirrorFromFile(pathToJarFile, domainModelPackages, uploadFileType);
    }
}
