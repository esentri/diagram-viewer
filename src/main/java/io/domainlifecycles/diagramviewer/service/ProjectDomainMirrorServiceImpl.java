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
import io.domainlifecycles.diagramviewer.util.CompressedJson;
import io.domainlifecycles.diagramviewer.util.DomainModelUtils;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.UploadFileType;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.serialize.DomainSerializer;
import io.domainlifecycles.staticanalysis.DomainCalls;
import io.domainlifecycles.staticanalysis.serialize.DomainCallsSerializer;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stores and loads the domain model of a project: its domain mirror and, optionally, the static analysis
 * result ({@code DomainCalls}).
 * <p>
 * Both are stored as gzip-compressed JSON ({@link CompressedJson}) and deserialized directly from the
 * decompressing stream, so the uncompressed JSON - several gigabytes for a large domain model - is never
 * held in memory as a whole. Projects last uploaded before the compressed storage was introduced are
 * still read from the legacy uncompressed columns, and switch to the compressed storage with their next
 * upload.
 */
@Service
public class ProjectDomainMirrorServiceImpl implements ProjectDomainMirrorService {

    private final RegenerateDiagramsJobService regenerateDiagramsJobService;
    private final ProjectDomainMirrorRepository repository;
    private final DomainSerializer serializer;
    private final DomainCallsSerializer domainCallsSerializer;

    public ProjectDomainMirrorServiceImpl(
            RegenerateDiagramsJobService regenerateDiagramsJobService,
            ProjectDomainMirrorRepository repository,
            DomainSerializer serializer,
            DomainCallsSerializer domainCallsSerializer) {
        this.regenerateDiagramsJobService = regenerateDiagramsJobService;
        this.repository = repository;
        this.serializer = serializer;
        this.domainCallsSerializer = domainCallsSerializer;
    }

    @Override
    public ProjectDomainMirror getByProjectId(UUID projectId) {
        return repository.findByProjectId(projectId)
            .orElseThrow(() -> DiagramViewerException.fail(String.format("No DomainTypeMirror found for project with id '%s'.", projectId)));
    }

    @Override
    public DomainMirror getDomainMirror(UUID projectId) {
        Optional<byte[]> compressed = repository.findDomainMirrorGzByProjectId(projectId);
        if (compressed.isPresent()) {
            return deserializeMirror(compressed.get());
        }
        return repository.findLegacyDomainMirrorByProjectId(projectId)
            .orElseThrow(() -> DiagramViewerException.fail(String.format("No DomainMirror found for project with id '%s'.", projectId)));
    }

    @Override
    public Optional<DomainCalls> loadDomainCalls(UUID projectId, DomainMirror domainMirror) {
        Optional<byte[]> compressed = repository.findDomainCallsGzByProjectId(projectId);
        if (compressed.isPresent()) {
            try (InputStream json = CompressedJson.decompress(compressed.get())) {
                return Optional.of(domainCallsSerializer.deserialize(json, domainMirror));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return repository.findLegacyDomainCallsJsonByProjectId(projectId)
            .map(json -> domainCallsSerializer.deserialize(json, domainMirror));
    }

    @Override
    public boolean hasDomainCalls(UUID projectId) {
        return repository.existsDomainCallsByProjectId(projectId);
    }

    @Override
    @Transactional
    public DomainMirror createOrUpdate(Project project, Set<String> domainModelPackages, Path pathToFile, UploadFileType uploadFileType) {
        DomainMirror domainMirror = generateDomainMirror(pathToFile, domainModelPackages, uploadFileType);
        byte[] domainMirrorGz = CompressedJson.compress(out -> serializer.serialize(domainMirror, out));
        CompressedJson.checkStorable(domainMirrorGz, "domain mirror");
        createOrUpdateCompressed(project, domainMirrorGz, null);
        return domainMirror;
    }

    @Override
    @Transactional
    public void createOrUpdateCompressed(Project project, byte[] domainMirrorGz, byte[] domainCallsGz) {
        if (repository.existsByProjectId(project.getId())) {
            repository.updateCompressed(project.getId(), domainMirrorGz, domainCallsGz);
            regenerateDiagramsJobService.create(project);
        } else {
            repository.insertCompressed(UUID.randomUUID(), project.getId(), domainMirrorGz, domainCallsGz);
        }
    }

    @Override
    @Transactional
    public void delete(UUID projectId) {
        repository.deleteByProjectIdWithoutLoading(projectId);
    }

    private DomainMirror deserializeMirror(byte[] compressed) {
        try (InputStream json = CompressedJson.decompress(compressed)) {
            return serializer.deserialize(json);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private DomainMirror generateDomainMirror(Path pathToJarFile, Set<String> domainModelPackages, UploadFileType uploadFileType) {
        return DomainModelUtils.initializeDomainMirrorFromFile(pathToJarFile, domainModelPackages, uploadFileType);
    }
}
