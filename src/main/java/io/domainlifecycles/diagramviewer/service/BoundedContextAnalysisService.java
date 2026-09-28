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

import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.util.DomainModelUtils;
import io.domainlifecycles.mirror.api.BoundedContextMirror;
import io.domainlifecycles.mirror.api.DomainCommandMirror;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainType;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.mirror.api.ReadModelMirror;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Creates a starting point for exploring a project: per Bounded Context a directory with
 * <ul>
 *     <li>a diagram of the Bounded Context's aggregates only,</li>
 *     <li>a sub directory "Read Models" with a diagram per top level read model - one contained in no other read model
 *     -, showing what leads into it (backward flow); a contained read model is shown in the diagram of the read model
 *     containing it,</li>
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
    public static final String AGGREGATES_DIAGRAM_NAME = "Aggregates";

    private static final Logger LOGGER = LoggerFactory.getLogger(BoundedContextAnalysisService.class);

    private final ProjectModelCache projectModelCache;
    private final DiagramService diagramService;
    private final DiagramDirectoryService diagramDirectoryService;
    /** one analysis at a time: analyses of the same project would otherwise race for the same diagram names */
    private final ExecutorService analysisExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "bounded-context-analysis");
        thread.setDaemon(true);
        return thread;
    });

    public BoundedContextAnalysisService(ProjectModelCache projectModelCache,
                                         DiagramService diagramService,
                                         DiagramDirectoryService diagramDirectoryService) {
        this.projectModelCache = projectModelCache;
        this.diagramService = diagramService;
        this.diagramDirectoryService = diagramDirectoryService;
    }

    @PreDestroy
    void shutdownAnalysisExecutor() {
        analysisExecutor.shutdownNow();
    }

    /**
     * Runs {@link #analyze(Project, ProgressListener)} in the background, so that the user interface stays responsive
     * - creating the diagrams of a large project takes a while.
     *
     * @param project  the project to analyze
     * @param listener informed about the progress, on the analysis thread
     * @return the outcome, once the diagrams are created (their images are rendered afterwards)
     */
    public CompletableFuture<Result> analyzeAsync(Project project, ProgressListener listener) {
        return CompletableFuture.supplyAsync(() -> analyze(project, listener), analysisExecutor);
    }

    /**
     * Informed while an analysis creates diagrams.
     */
    @FunctionalInterface
    public interface ProgressListener {

        ProgressListener NONE = (done, total, diagramName) -> { };

        /**
         * @param done        the number of diagrams handled so far (created or skipped)
         * @param total       the number of diagrams the analysis handles in total
         * @param diagramName the diagram just handled
         */
        void diagramHandled(int done, int total, String diagramName);
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
        return analyze(project, ProgressListener.NONE);
    }

    /**
     * @param project  the project to analyze
     * @param listener informed about the progress
     * @return what was created
     */
    public Result analyze(Project project, ProgressListener listener) {
        ProjectModel model = projectModelCache.get(project.getId());
        DomainMirror domainMirror = model.domainMirror();
        boolean flowsAvailable = model.domainCallsAvailable();

        List<BoundedContextMirror> boundedContexts = domainMirror.getAllBoundedContextMirrors().stream()
            .sorted(Comparator.comparing(BoundedContextAnalysisService::label, String.CASE_INSENSITIVE_ORDER))
            .toList();
        Set<String> containedReadModels = containedReadModelTypeNames(domainMirror);
        int total = boundedContexts.stream()
            .mapToInt(boundedContext -> (boundedContext.getAggregateRoots().isEmpty() ? 0 : 1)
                + (flowsAvailable
                    ? topLevelReadModels(boundedContext, containedReadModels).size() + boundedContext.getDomainCommands().size()
                    : 0))
            .sum();
        Analysis analysis = new Analysis(project, domainMirror, containedReadModels, listener, total);
        for (BoundedContextMirror boundedContext : boundedContexts) {
            analysis.analyze(boundedContext, flowsAvailable);
        }
        LOGGER.info("Analyzed {} bounded contexts of project '{}': {} diagrams created, {} already existing.",
            boundedContexts.size(), project.getName(), analysis.created, analysis.skipped);
        return new Result(boundedContexts.size(), analysis.created, analysis.skipped, !flowsAvailable,
            List.copyOf(analysis.renderings));
    }

    /**
     * The read models of a Bounded Context contained in no other read model.
     */
    static List<ReadModelMirror> topLevelReadModels(BoundedContextMirror boundedContext, Set<String> containedReadModels) {
        return sortedByName(boundedContext.getReadModels()).stream()
            .filter(readModel -> !containedReadModels.contains(readModel.getTypeName()))
            .toList();
    }

    /**
     * The read models contained in another read model - as field, {@code Optional} or collection. A read model
     * containing itself, e.g. as tree, is not contained by that alone.
     */
    static Set<String> containedReadModelTypeNames(DomainMirror domainMirror) {
        return domainMirror.getAllReadModelMirrors().stream()
            .flatMap(readModel -> readModel.getAllFields().stream()
                .filter(field -> DomainType.READ_MODEL.equals(field.getType().getDomainType()))
                .map(field -> field.getType().getTypeName())
                .filter(typeName -> !typeName.equals(readModel.getTypeName())))
            .collect(Collectors.toSet());
    }

    private static String label(BoundedContextMirror boundedContext) {
        return boundedContext.getName().orElse(boundedContext.getPackageName());
    }

    private final class Analysis {

        private final Project project;
        private final DomainMirror domainMirror;
        private final Set<String> containedReadModels;
        private final ProgressListener listener;
        private final int total;
        private final List<CompletableFuture<DiagramRendering.Result>> renderings = new ArrayList<>();
        private Map<String, Set<String>> processingMethodsByCommand;
        private int created;
        private int skipped;

        private Analysis(Project project, DomainMirror domainMirror, Set<String> containedReadModels,
                         ProgressListener listener, int total) {
            this.project = project;
            this.domainMirror = domainMirror;
            this.containedReadModels = containedReadModels;
            this.listener = listener;
            this.total = total;
        }

        private void analyze(BoundedContextMirror boundedContext, boolean flowsAvailable) {
            String label = label(boundedContext);
            DiagramDirectory directory = diagramDirectoryService.findOrCreate(project, null, label);

            if (!boundedContext.getAggregateRoots().isEmpty()) {
                createDiagram(directory, AGGREGATES_DIAGRAM_NAME,
                    new DomainModelVisibility()
                        .replaceIncludedBoundedContextPackages(Set.of(boundedContext.getPackageName())),
                    aggregatesOnly());
            }
            if (!flowsAvailable) {
                return;
            }

            List<ReadModelMirror> readModels = topLevelReadModels(boundedContext, containedReadModels);
            if (!readModels.isEmpty()) {
                DiagramDirectory readModelsDirectory = diagramDirectoryService.findOrCreate(project, directory, READ_MODELS_DIRECTORY);
                Map<String, String> names = diagramNames(readModels, boundedContext);
                readModels.forEach(readModel -> createDiagram(readModelsDirectory,
                    names.get(readModel.getTypeName()),
                    new DomainModelVisibility().replaceIncludeFlowsTo(Set.of(readModel.getTypeName())),
                    DiagramStylingConfiguration.builder().build()));
            }

            List<DomainCommandMirror> commands = sortedByName(boundedContext.getDomainCommands());
            if (!commands.isEmpty()) {
                DiagramDirectory commandsDirectory = diagramDirectoryService.findOrCreate(project, directory, COMMANDS_DIRECTORY);
                Map<String, String> names = diagramNames(commands, boundedContext);
                commands.forEach(command -> createDiagram(commandsDirectory,
                    names.get(command.getTypeName()),
                    new DomainModelVisibility()
                        .replaceIncludeFlowsFrom(Set.of(command.getTypeName()))
                        .replaceIncludeFlowsTo(methodsProcessing(command)),
                    DiagramStylingConfiguration.builder().build()));
            }
        }

        private void createDiagram(DiagramDirectory directory, String name, DomainModelVisibility visibility,
                                   DiagramStylingConfiguration styling) {
            // names are unique within a directory, see DiagramServiceImpl
            boolean exists = directory.getDiagrams().stream().anyMatch(diagram -> name.equals(diagram.getName()));
            if (exists) {
                skipped++;
            } else {
                DiagramRendering rendering = diagramService.createAsync(project, directory, name, visibility, styling);
                renderings.add(rendering.image());
                created++;
            }
            listener.diagramHandled(created + skipped, total, name);
        }

        /**
         * The backward flow targets for a command: every method processing it, as {@code Type#method}.
         */
        private Set<String> methodsProcessing(DomainCommandMirror command) {
            if (processingMethodsByCommand == null) {
                // indexed once: resolving the processed commands of every method for every command took the
                // analysis of large projects tens of seconds
                processingMethodsByCommand = new HashMap<>();
                for (DomainTypeMirror type : domainMirror.getAllDomainTypeMirrors()) {
                    if (type.getTypeName().startsWith(DomainModelUtils.DOMAINLIFECYCLES_PACKAGE_NAME)) {
                        continue;
                    }
                    type.getMethods().forEach(method -> method.getProcessedCommands().forEach(processed ->
                        processingMethodsByCommand.computeIfAbsent(processed.getTypeName(), key -> new LinkedHashSet<>())
                            .add(type.getTypeName() + "#" + method.getName())));
                }
            }
            return processingMethodsByCommand.getOrDefault(command.getTypeName(), Set.of());
        }
    }

    /**
     * The diagram names for types sharing one directory: their simple names - or, where several share one, the simple
     * name followed by the package relative to the Bounded Context. The same for every run, so that analyzing again
     * finds the diagrams created before.
     */
    static Map<String, String> diagramNames(List<? extends DomainTypeMirror> types, BoundedContextMirror boundedContext) {
        Map<String, Long> countBySimpleName = types.stream()
            .collect(Collectors.groupingBy(type -> shortName(type.getTypeName()), Collectors.counting()));
        Map<String, String> names = new HashMap<>();
        for (DomainTypeMirror type : types) {
            String simpleName = shortName(type.getTypeName());
            names.put(type.getTypeName(), countBySimpleName.get(simpleName) == 1
                ? simpleName
                : simpleName + " (" + relativePackage(type.getTypeName(), boundedContext.getPackageName()) + ")");
        }
        return names;
    }

    private static String relativePackage(String typeName, String boundedContextPackage) {
        String packageName = typeName.substring(0, typeName.lastIndexOf('.'));
        return packageName.startsWith(boundedContextPackage + ".")
            ? packageName.substring(boundedContextPackage.length() + 1)
            : packageName;
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
