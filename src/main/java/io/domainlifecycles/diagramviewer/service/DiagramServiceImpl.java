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
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramTypeNote;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.repository.DiagramTypeNoteRepository;
import io.domainlifecycles.diagramviewer.rest.kroki.KrokiClient;
import io.domainlifecycles.diagramviewer.util.DiagramFileUtils;
import io.domainlifecycles.diagramviewer.util.DiagrammerUtils;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.staticanalysis.DomainCalls;
import jakarta.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;
import java.util.Map;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DiagramServiceImpl implements DiagramService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiagramServiceImpl.class);
    public static final String SVG_FILE_SUFFIX = DiagramFileUtils.SVG_FILE_SUFFIX;

    private final String diagramsLocation;
    private final ProjectModelCache projectModelCache;
    private final DiagramRepository repository;
    private final DiagramTypeNoteRepository noteRepository;
    private final KrokiClient krokiClient;
    private final int largeDiagramClasses;
    private final long previewLimitBytes;
    private final ExecutorService renderingExecutor;
    /** per diagram the number of the latest background rendering requested, see {@link #renderInBackground} */
    private final Map<UUID, AtomicLong> latestRenderings = new ConcurrentHashMap<>();

    public DiagramServiceImpl(
        @Value("${diagrams.location}") String diagramsLocation,
        ProjectModelCache projectModelCache,
        DiagramRepository repository,
        DiagramTypeNoteRepository noteRepository,
        KrokiClient krokiClient,
        @Value("${diagrams.rendering.threads:2}") int renderingThreads,
        @Value("${diagrams.largeDiagramClasses:1000}") int largeDiagramClasses,
        @Value("${diagrams.cardPreviewMaxKilobytes:1024}") long cardPreviewMaxKilobytes
    ) {
        this.diagramsLocation = diagramsLocation;
        this.projectModelCache = projectModelCache;
        this.repository = repository;
        this.noteRepository = noteRepository;
        this.krokiClient = krokiClient;
        this.largeDiagramClasses = largeDiagramClasses;
        this.previewLimitBytes = cardPreviewMaxKilobytes * 1024;
        AtomicInteger threadNumber = new AtomicInteger();
        this.renderingExecutor = Executors.newFixedThreadPool(renderingThreads, runnable -> {
            Thread thread = new Thread(runnable, "diagram-rendering-" + threadNumber.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
    }

    @PreDestroy
    public void shutdownRenderingExecutor() {
        renderingExecutor.shutdownNow();
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
            projectModelCache.get(projectId).domainMirror(),
            domainCallsIfNeeded(updatedDiagram),
            updatedDiagram);
        return updatedDiagram;
    }

    @Override
    public Diagram rename(Diagram diagram, String newName) {
        // the image is named after the diagram's id, see DiagramFileUtils#imagePath
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
            projectModelCache.get(projectId).domainMirror(),
            domainCallsIfNeeded(diagram),
            diagram);
        project.addDiagram(diagram);

        return diagram;
    }

    @Override
    public DiagramRendering updateModelAndImageAsync(Diagram diagram) {
        diagram.setChangedAt(Instant.now());
        final Diagram updatedDiagram = save(diagram);
        return new DiagramRendering(updatedDiagram, renderInBackground(updatedDiagram));
    }

    @Override
    public DiagramRendering createAsync(Project project,
                                        String name,
                                        DomainModelVisibility visibility,
                                        DiagramStylingConfiguration diagramStylingConfiguration) {
        return createAsync(project, null, name, visibility, diagramStylingConfiguration);
    }

    @Override
    public DiagramRendering createAsync(Project project,
                                        DiagramDirectory directory,
                                        String name,
                                        DomainModelVisibility visibility,
                                        DiagramStylingConfiguration diagramStylingConfiguration) {
        Diagram diagram = Diagram.builder()
            .name(name)
            .domainModelVisibility(visibility)
            .diagramStylingConfiguration(diagramStylingConfiguration)
            .project(project)
            .diagramDirectory(directory)
            .build();

        save(diagram);
        project.addDiagram(diagram);
        if (directory != null) {
            directory.getDiagrams().add(diagram);
        }
        return new DiagramRendering(diagram, renderInBackground(diagram));
    }

    /**
     * Renders the diagram's image on the rendering executor. Each request gets a number per diagram; a request
     * that is no longer the latest one when it starts is skipped, and one that got superseded while rendering
     * drops its result instead of overwriting the newer image. Saving is serialized per diagram for the same
     * reason. Failures of superseded requests are not reported either.
     */
    private CompletableFuture<DiagramRendering.Result> renderInBackground(Diagram diagram) {
        // the user interface may change the entity for the next request while this one is rendered
        final Diagram snapshot = diagram.toBuilder().build();
        final AtomicLong latest = latestRenderings.computeIfAbsent(diagram.getId(), id -> new AtomicLong());
        final long request = latest.incrementAndGet();
        return CompletableFuture.supplyAsync(() -> {
            if (latest.get() != request) {
                return DiagramRendering.Result.SUPERSEDED;
            }
            try {
                RenderedImage image = renderImage(
                    projectModelCache.get(snapshot.getProject().getId()).domainMirror(),
                    domainCallsIfNeeded(snapshot),
                    snapshot);
                synchronized (latest) {
                    if (latest.get() != request) {
                        return DiagramRendering.Result.SUPERSEDED;
                    }
                    saveImage(snapshot, image.svg());
                }
                return new DiagramRendering.Result(true, image.classCount(), image.classCount() > largeDiagramClasses);
            } catch (RuntimeException e) {
                if (latest.get() != request) {
                    return DiagramRendering.Result.SUPERSEDED;
                }
                throw e;
            }
        }, renderingExecutor);
    }

    /**
     * The static analysis result is only needed to render a diagram restricted to a flow - requesting it
     * only then keeps it from being loaded at all for projects whose diagrams use no flow filter.
     */
    private DomainCalls domainCallsIfNeeded(Diagram diagram) {
        if (diagram.getDomainModelVisibility() == null || !diagram.getDomainModelVisibility().hasFlowSettings()) {
            return null;
        }
        return projectModelCache.get(diagram.getProject().getId()).domainCalls().orElse(null);
    }

    private Diagram save(Diagram diagram) {
        final String fileName = diagram.getName();

        if (nameTakenInDirectory(diagram)) {
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
        saveImage(diagram, renderImage(domainMirror, domainCalls, diagram).svg());
    }

    private RenderedImage renderImage(DomainMirror domainMirror, DomainCalls domainCalls, Diagram diagram) {

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

        int classCount = DiagrammerUtils.countClasses(nomnoml);
        if (classCount > largeDiagramClasses) {
            LOGGER.warn("Diagram '{}' contains {} classes, converting it may take long.", diagram.getName(), classCount);
        }
        return new RenderedImage(krokiClient.convert(nomnoml), classCount);
    }

    private void saveImage(Diagram diagram, byte[] diagramFileContents) {
        Path diagramPath = DiagramFileUtils.imagePath(diagramsLocation, diagram);
        try {
            FileIOUtils.saveFile(diagramPath.toAbsolutePath(), new ByteArrayInputStream(diagramFileContents));
        } catch (IOException e) {
            throw DiagramViewerException.fail(String.format("Could not save diagram to '%s'.", diagramsLocation), e);
        }
    }

    private record RenderedImage(byte[] svg, int classCount) {
    }

    @Override
    public long imageSize(Diagram diagram) {
        Path diagramPath = DiagramFileUtils.imagePath(diagramsLocation, diagram);
        try {
            return Files.size(diagramPath);
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public long previewLimitBytes() {
        return previewLimitBytes;
    }

    /**
     * Names are unique within a directory of a project, or among the diagrams of a project without directory - the
     * diagram view and the stored image are addressed by the diagram's id.
     */
    private boolean nameTakenInDirectory(Diagram diagram) {
        UUID projectId = diagram.getProject() == null ? null : diagram.getProject().getId();
        UUID directoryId = directoryIdOf(diagram);
        return repository.findByProjectIdAndName(projectId, diagram.getName()).stream()
            .filter(other -> diagram.getId() == null || !diagram.getId().equals(other.getId()))
            .anyMatch(other -> Objects.equals(directoryIdOf(other), directoryId));
    }

    private static UUID directoryIdOf(Diagram diagram) {
        return diagram.getDiagramDirectory() == null ? null : diagram.getDiagramDirectory().getId();
    }
}
