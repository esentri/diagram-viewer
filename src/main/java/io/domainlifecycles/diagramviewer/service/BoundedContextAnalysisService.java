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
import io.domainlifecycles.diagramviewer.util.DomainModelUtils;
import io.domainlifecycles.mirror.api.BoundedContextMirror;
import io.domainlifecycles.mirror.api.DomainCommandMirror;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.mirror.api.ReadModelMirror;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Creates a starting point for exploring a project: per Bounded Context a directory with
 * <ul>
 *     <li>a diagram of the Bounded Context's aggregates only,</li>
 *     <li>a sub directory "Read Models" with a diagram per read model, showing what leads into it (backward flow),</li>
 *     <li>a sub directory "Commands" with a diagram per command, showing the flow it triggers (forward) together with
 *     what leads into the methods processing it (backward - a command itself cannot be a backward flow target).</li>
 * </ul>
 * The flow diagrams need the project's static analysis result; without it, only the aggregate diagrams are created.
 * Running the analysis again only adds what is missing: existing directories are reused and diagrams whose name
 * already exists in the project are left untouched, so changes made to them are kept.
 */
@Service
public class BoundedContextAnalysisService {

    public static final String READ_MODELS_DIRECTORY = "Read Models";
    public static final String COMMANDS_DIRECTORY = "Commands";
    static final String AGGREGATES_DIAGRAM_SUFFIX = "Aggregates";

    private static final Logger LOGGER = LoggerFactory.getLogger(BoundedContextAnalysisService.class);

    private final ProjectModelCache projectModelCache;
    private final DiagramService diagramService;
    private final DiagramDirectoryService diagramDirectoryService;

    public BoundedContextAnalysisService(ProjectModelCache projectModelCache,
                                         DiagramService diagramService,
                                         DiagramDirectoryService diagramDirectoryService) {
        this.projectModelCache = projectModelCache;
        this.diagramService = diagramService;
        this.diagramDirectoryService = diagramDirectoryService;
    }

    /**
     * The outcome of an analysis.
     *
     * @param boundedContexts   the number of analyzed Bounded Contexts
     * @param createdDiagrams   the number of diagrams created
     * @param skippedDiagrams   the number of diagrams not created, since a diagram of that name already existed
     * @param flowsSkipped      {@code true} if no static analysis result was uploaded, so that no read model and
     *                          command diagrams could be created
     * @param renderings        the background renderings of the created diagrams' images
     */
    public record Result(int boundedContexts,
                         int createdDiagrams,
                         int skippedDiagrams,
                         boolean flowsSkipped,
                         List<CompletableFuture<DiagramRendering.Result>> renderings) {
    }

    /**
     * @param project the project to analyze
     * @return what was created
     */
    public Result analyze(Project project) {
        ProjectModel model = projectModelCache.get(project.getId());
        DomainMirror domainMirror = model.domainMirror();
        boolean flowsAvailable = model.domainCallsAvailable();
        Analysis analysis = new Analysis(project, domainMirror);

        List<BoundedContextMirror> boundedContexts = domainMirror.getAllBoundedContextMirrors().stream()
            .sorted(Comparator.comparing(BoundedContextAnalysisService::label, String.CASE_INSENSITIVE_ORDER))
            .toList();
        for (BoundedContextMirror boundedContext : boundedContexts) {
            analysis.analyze(boundedContext, flowsAvailable);
        }
        LOGGER.info("Analyzed {} bounded contexts of project '{}': {} diagrams created, {} already existing.",
            boundedContexts.size(), project.getName(), analysis.created, analysis.skipped);
        return new Result(boundedContexts.size(), analysis.created, analysis.skipped, !flowsAvailable,
            List.copyOf(analysis.renderings));
    }

    private static String label(BoundedContextMirror boundedContext) {
        return boundedContext.getName().orElse(boundedContext.getPackageName());
    }

    private final class Analysis {

        private final Project project;
        private final DomainMirror domainMirror;
        private final List<CompletableFuture<DiagramRendering.Result>> renderings = new ArrayList<>();
        private int created;
        private int skipped;

        private Analysis(Project project, DomainMirror domainMirror) {
            this.project = project;
            this.domainMirror = domainMirror;
        }

        private void analyze(BoundedContextMirror boundedContext, boolean flowsAvailable) {
            String label = label(boundedContext);
            DiagramDirectory directory = diagramDirectoryService.findOrCreate(project, null, label);

            if (!boundedContext.getAggregateRoots().isEmpty()) {
                createDiagram(directory, label + " - " + AGGREGATES_DIAGRAM_SUFFIX,
                    new DomainModelVisibility()
                        .replaceIncludedBoundedContextPackages(Set.of(boundedContext.getPackageName())),
                    aggregatesOnly());
            }
            if (!flowsAvailable) {
                return;
            }

            List<ReadModelMirror> readModels = sortedByName(boundedContext.getReadModels());
            if (!readModels.isEmpty()) {
                DiagramDirectory readModelsDirectory = diagramDirectoryService.findOrCreate(project, directory, READ_MODELS_DIRECTORY);
                readModels.forEach(readModel -> createDiagram(readModelsDirectory,
                    label + " - " + shortName(readModel.getTypeName()),
                    new DomainModelVisibility().replaceIncludeFlowsTo(Set.of(readModel.getTypeName())),
                    DiagramStylingConfiguration.builder().build()));
            }

            List<DomainCommandMirror> commands = sortedByName(boundedContext.getDomainCommands());
            if (!commands.isEmpty()) {
                DiagramDirectory commandsDirectory = diagramDirectoryService.findOrCreate(project, directory, COMMANDS_DIRECTORY);
                commands.forEach(command -> createDiagram(commandsDirectory,
                    label + " - " + shortName(command.getTypeName()),
                    new DomainModelVisibility()
                        .replaceIncludeFlowsFrom(Set.of(command.getTypeName()))
                        .replaceIncludeFlowsTo(methodsProcessing(command)),
                    DiagramStylingConfiguration.builder().build()));
            }
        }

        private void createDiagram(DiagramDirectory directory, String name, DomainModelVisibility visibility,
                                   DiagramStylingConfiguration styling) {
            boolean exists = project.getDiagrams().stream().anyMatch(diagram -> name.equals(diagram.getName()));
            if (exists) {
                skipped++;
                return;
            }
            DiagramRendering rendering = diagramService.createAsync(project, name, visibility, styling);
            Diagram diagram = rendering.diagram();
            diagramDirectoryService.add(directory, diagram);
            renderings.add(rendering.image());
            created++;
        }

        /**
         * The backward flow targets for a command: every method processing it, as {@code Type#method}.
         */
        private Set<String> methodsProcessing(DomainCommandMirror command) {
            return domainMirror.getAllDomainTypeMirrors().stream()
                .filter(type -> !type.getTypeName().startsWith(DomainModelUtils.DOMAINLIFECYCLES_PACKAGE_NAME))
                .flatMap(type -> type.getMethods().stream()
                    .filter(method -> method.getProcessedCommands().stream()
                        .anyMatch(processed -> processed.getTypeName().equals(command.getTypeName())))
                    .map(method -> type.getTypeName() + "#" + method.getName()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }

    private static String shortName(String typeName) {
        return typeName.substring(typeName.lastIndexOf('.') + 1);
    }

    private static <T extends DomainTypeMirror> List<T> sortedByName(Collection<T> mirrors) {
        return mirrors.stream()
            .sorted(Comparator.comparing(mirror -> shortName(mirror.getTypeName())))
            .toList();
    }

    /**
     * Shows the aggregates only - with their entities and value objects, but no services, events, commands or other
     * kinds of types.
     */
    private static DiagramStylingConfiguration aggregatesOnly() {
        DiagramStylingConfiguration styling = DiagramStylingConfiguration.builder().build();
        styling.setShowAggregates(true);
        styling.setShowDomainEvents(false);
        styling.setShowDomainCommands(false);
        styling.setShowDomainServices(false);
        styling.setShowApplicationServices(false);
        styling.setShowRepositories(false);
        styling.setShowReadModels(false);
        styling.setShowQueryHandlers(false);
        styling.setShowOutboundServices(false);
        styling.setShowUnspecifiedServiceKinds(false);
        styling.setShowNonDomainClasses(false);
        return styling;
    }
}
