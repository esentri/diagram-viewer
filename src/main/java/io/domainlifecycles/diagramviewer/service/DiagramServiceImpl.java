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
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramTypeNote;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.repository.DiagramTypeNoteRepository;
import io.domainlifecycles.diagramviewer.rest.kroki.KrokiClient;
import io.domainlifecycles.diagramviewer.util.DiagrammerUtils;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.staticanalysis.DomainCalls;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;

import java.util.List;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DiagramServiceImpl implements DiagramService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiagramServiceImpl.class);
    public static final String SVG_FILE_SUFFIX = ".svg";

    private final String diagramsLocation;
    private final SessionStorage sessionStorage;
    private final DiagramRepository repository;
    private final DiagramTypeNoteRepository noteRepository;
    private final KrokiClient krokiClient;

    public DiagramServiceImpl(
        @Value("${diagrams.location}") String diagramsLocation,
        SessionStorage sessionStorage,
        DiagramRepository repository,
        DiagramTypeNoteRepository noteRepository,
        KrokiClient krokiClient
    ) {
        this.diagramsLocation = diagramsLocation;
        this.sessionStorage = sessionStorage;
        this.repository = repository;
        this.noteRepository = noteRepository;
        this.krokiClient = krokiClient;
    }

    @Override
    public Set<Diagram> findAll(UUID projectId) {
        return repository.findByProjectId(projectId);
    }

    @Override
    public Diagram updateModel(Diagram diagram) {
        return save(diagram);
    }

    @Override
    public Diagram updateModelAndImage(Diagram diagram) {
        diagram.setChangedAt(Instant.now());
        final Diagram updatedDiagram = save(diagram);
        UUID projectId = diagram.getProject().getId();
        createAndSaveDiagramToFilesystem(
            sessionStorage.getDomainMirror(projectId),
            domainCallsIfNeeded(updatedDiagram),
            updatedDiagram);
        return updatedDiagram;
    }

    @Override
    public Diagram rename(Diagram diagram, String newName) {
        Path diagramPath = Path.of(diagramsLocation, diagram.getProject().getId().toString(), diagram.getName() + SVG_FILE_SUFFIX);
        FileIOUtils.renameFile(diagramPath, newName + SVG_FILE_SUFFIX);

        diagram.setName(newName);
        return updateModel(diagram);
    }

    @Override
    public Diagram create(Project project,
                          String name,
                          DomainModelVisibility visibility,
                          DiagramStylingConfiguration diagramStylingConfiguration) {

        Diagram diagram = Diagram.builder()
            .name(name)
            .domainModelVisibility(visibility)
            .diagramStylingConfiguration(diagramStylingConfiguration)
            .project(project)
            .build();

        save(diagram);
        UUID projectId = diagram.getProject().getId();
        createAndSaveDiagramToFilesystem(
            sessionStorage.getDomainMirror(projectId),
            domainCallsIfNeeded(diagram),
            diagram);
        project.addDiagram(diagram);

        return diagram;
    }

    /**
     * The static analysis result is only needed to render a diagram restricted to a flow - requesting it
     * only then keeps it from being loaded at all for projects whose diagrams use no flow filter.
     */
    private DomainCalls domainCallsIfNeeded(Diagram diagram) {
        if (diagram.getDomainModelVisibility() == null || !diagram.getDomainModelVisibility().hasFlowSettings()) {
            return null;
        }
        return sessionStorage.getDomainCalls(diagram.getProject().getId()).orElse(null);
    }

    private Diagram save(Diagram diagram) {
        final String fileName = diagram.getName();

        if(diagramWithNameExists(diagram) && diagramNameHasChanged(diagram)) {
            throw DiagramViewerException.fail(String.format("Diagram with name '%s' already exists. Please choose a different name.",
                fileName));
        }

        return repository.save(diagram);
    }

    @Override
    public void deleteFilesFromFilesystem(String projectId) {
        Path projectDiagramsDirectory = Path.of(diagramsLocation, projectId);

        try {
            FileIOUtils.deleteDirectoryRecursively(projectDiagramsDirectory);
        } catch (IOException e) {
            throw DiagramViewerException.fail(String.format("Could not delete diagrams of project '%s'.", e),
                projectId);
        }
    }

    @Override
    public void createAndSaveDiagramToFilesystem(DomainMirror domainMirror, DomainCalls domainCalls, Diagram diagram) {

        final String nomnoml;
        List<DiagramTypeNote> notes = noteRepository.findByDiagramId(diagram.getId());

        try {
            nomnoml = DiagrammerUtils.generateNomnoml(
                domainMirror,
                diagram.getDiagramStylingConfiguration(),
                diagram.getDomainModelVisibility(),
                notes,
                domainCalls
            );
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw DiagramViewerException.fail(e.getMessage(), e);
        }

        byte[] diagramFileContents = krokiClient.convert(nomnoml);

        Path diagramPath = Path.of(diagramsLocation, diagram.getProject().getId().toString(), diagram.getName() + SVG_FILE_SUFFIX);
        try {
            FileIOUtils.saveFile(diagramPath.toAbsolutePath(), new ByteArrayInputStream(diagramFileContents));
        } catch (IOException e) {
            throw DiagramViewerException.fail(String.format("Could not save diagram to '%s'.", diagramsLocation), e);
        }
    }

    private boolean diagramWithNameExists(Diagram diagram) {
        Optional<Diagram> diagramWithName = repository.findByName(diagram.getName());
        return diagramWithName.isPresent() && Objects.equals(diagram.getName(), diagramWithName.get().getName());
    }

    private boolean diagramNameHasChanged(Diagram diagram) {
        if(diagram.getId() == null) return true;
        Optional<Diagram> oldDiagram = repository.findById(diagram.getId());
        return oldDiagram.isPresent() && !Objects.equals(oldDiagram.get().getName(), diagram.getName());
    }
}
