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

package io.domainlifecycles.diagramviewer.util;

import io.domainlifecycles.mirror.api.BoundedContextMirror;
import io.domainlifecycles.mirror.api.DomainCommandMirror;
import io.domainlifecycles.mirror.api.DomainEventMirror;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainType;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.mirror.api.MethodMirror;
import io.domainlifecycles.staticanalysis.DomainCallFlowAnalyzer;
import io.domainlifecycles.staticanalysis.DomainCalls;
import io.domainlifecycles.staticanalysis.DomainMethod;
import io.domainlifecycles.staticanalysis.Flow;
import io.domainlifecycles.staticanalysis.FlowConfig;
import io.domainlifecycles.staticanalysis.Step;
import io.domainlifecycles.staticanalysis.StepKind;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The flows a diagram is restricted to, as text: the backward part ("what leads into it") above, the forward part
 * ("what it leads to") below. Both are read from top to bottom in call order, so the backward part is an upside-down
 * tree - its branches open upwards and its target is at the bottom.
 * <p>
 * Each flow is a tree: a node reached on several ways is expanded once, all further occurrences refer to it. The flows
 * are resolved once; {@link #render(Options)} only lays them out, so changing the options is cheap.
 */
public final class FlowText {

    private static final String METHOD_SEPARATOR = "#";

    /**
     * How to lay out the flows.
     *
     * @param hideAccessors whether to hide the leaf calls reading values - accessors of value objects, identities,
     *                      enums, commands, events, read models, and getters of entities and other classes
     * @param maxDepth      the number of steps shown from a start or target, {@code 0} for all
     * @param search        if not blank, only the paths leading to a step containing it are shown
     */
    public record Options(boolean hideAccessors, int maxDepth, String search) {

        boolean searching() {
            return search != null && !search.isBlank();
        }
    }

    /**
     * One line of the text.
     *
     * @param kind  what the line shows
     * @param text  the line, with its tree connectors
     * @param match whether the line contains the searched text
     */
    public record Line(LineKind kind, String text, boolean match) {
    }

    public enum LineKind {
        HEADING, STEP, NOTE, BLANK
    }

    private final DomainMirror domainMirror;
    private final List<Node> backwardRoots;
    private final List<Node> forwardRoots;
    private final Set<String> startKeys = new HashSet<>();
    /** the node expanding a step, per direction: other occurrences of the step lead on through it */
    private final Map<String, Node> expansions = new HashMap<>();
    private final List<String> unresolved = new ArrayList<>();
    private final String startContext;
    private final String commonContextPackage;

    /**
     * Resolves the flows the way the diagram does: a command or event starts the flow it triggers, {@code Type#method}
     * restricts a type to the overloads of one method, any other type stands for all of its methods; backward, an event
     * resolves to the methods publishing it, and any other type to everything leading into it.
     *
     * @param domainMirror the mirror of the domain
     * @param domainCalls  the result of the static analysis of the domain
     * @param flowsFrom    the forward starting points of the diagram
     * @param flowsTo      the backward targets of the diagram
     */
    public FlowText(DomainMirror domainMirror, DomainCalls domainCalls, Collection<String> flowsFrom,
                    Collection<String> flowsTo) {
        this.domainMirror = domainMirror;
        DomainCallFlowAnalyzer analyzer = new DomainCallFlowAnalyzer(domainMirror, domainCalls, FlowConfig.defaults());
        this.backwardRoots = distinct(flowsTo.stream().flatMap(point -> backward(analyzer, point).stream()).toList());
        this.forwardRoots = distinct(flowsFrom.stream().flatMap(point -> forward(analyzer, point).stream()).toList());
        forwardRoots.forEach(root -> startKeys.add(root.step.nodeKey()));
        backwardRoots.forEach(root -> indexExpansions(root, true));
        forwardRoots.forEach(root -> indexExpansions(root, false));
        this.commonContextPackage = commonContextPackage(domainMirror.getAllBoundedContextMirrors());
        String firstPoint = !flowsFrom.isEmpty() ? flowsFrom.iterator().next()
            : !flowsTo.isEmpty() ? flowsTo.iterator().next() : "";
        this.startContext = context(typeNameOf(firstPoint));
    }

    /**
     * @param options how to lay out the flows
     * @return the lines of the text
     */
    public List<Line> render(Options options) {
        Rendering dryRun = new Rendering(options, Map.of());
        dryRun.render();
        Map<String, Integer> referenceIds = new HashMap<>();
        for (String key : dryRun.expansionOrder) {
            if (dryRun.occurrences.getOrDefault(key, 0) > 1) {
                referenceIds.put(key, referenceIds.size() + 1);
            }
        }
        Rendering rendering = new Rendering(options, referenceIds);
        rendering.render();
        return rendering.lines;
    }

    /**
     * @param lines rendered lines
     * @return them as plain text
     */
    public static String toText(List<Line> lines) {
        return lines.stream().map(Line::text).collect(Collectors.joining("\n", "", "\n"));
    }

    // ---------------------------------------------------------------------
    // Resolving
    // ---------------------------------------------------------------------

    private List<Node> forward(DomainCallFlowAnalyzer analyzer, String point) {
        Optional<DomainTypeMirror> type = domainMirror.getDomainTypeMirror(typeNameOf(point));
        String methodName = methodNameOf(point);
        if (type.isEmpty()) {
            unresolved.add(point);
            return List.of();
        }
        if (methodName == null && type.get() instanceof DomainCommandMirror command) {
            return List.of(tree(analyzer.flowFrom(command)));
        }
        if (methodName == null && type.get() instanceof DomainEventMirror event) {
            return List.of(tree(analyzer.flowFrom(event)));
        }
        return methods(type.get(), methodName).stream()
            .map(method -> tree(analyzer.flowFrom(new DomainMethod(type.get().getTypeName(), method))))
            .toList();
    }

    private List<Node> backward(DomainCallFlowAnalyzer analyzer, String point) {
        Optional<DomainTypeMirror> type = domainMirror.getDomainTypeMirror(typeNameOf(point));
        String methodName = methodNameOf(point);
        // a command cannot be a backward target, the diagram rejects it too
        if (type.isEmpty() || methodName == null && type.get() instanceof DomainCommandMirror) {
            unresolved.add(point);
            return List.of();
        }
        if (methodName == null && type.get() instanceof DomainEventMirror event) {
            return List.of(tree(analyzer.flowTo(event)));
        }
        if (methodName == null) {
            return List.of(tree(analyzer.flowTo(type.get())));
        }
        return methods(type.get(), methodName).stream()
            .map(method -> tree(analyzer.flowTo(new DomainMethod(type.get().getTypeName(), method))))
            .toList();
    }

    private List<MethodMirror> methods(DomainTypeMirror type, String methodName) {
        List<MethodMirror> methods = type.getMethods().stream()
            .filter(method -> methodName == null || method.getName().equals(methodName))
            .toList();
        if (methods.isEmpty()) {
            unresolved.add(type.getTypeName() + (methodName == null ? "" : METHOD_SEPARATOR + methodName));
        }
        return methods;
    }

    private static String typeNameOf(String point) {
        int separator = point.indexOf(METHOD_SEPARATOR);
        return separator < 0 ? point : point.substring(0, separator);
    }

    private static String methodNameOf(String point) {
        int separator = point.indexOf(METHOD_SEPARATOR);
        return separator < 0 ? null : point.substring(separator + METHOD_SEPARATOR.length());
    }

    // ---------------------------------------------------------------------
    // Trees
    // ---------------------------------------------------------------------

    private static final class Node {
        private final Step step;
        private final List<Node> children = new ArrayList<>();
        /** how often the parent reaches this node in the same way, e.g. the number of call sites */
        private int times = 1;

        private Node(Step step) {
            this.step = step;
        }

        private boolean isLeaf() {
            return children.stream().allMatch(child -> child.step.cyclic());
        }
    }

    /**
     * A flow reports every incoming edge of a node, but expands each node once; its steps therefore form a tree along
     * {@link Step#from()}. Equal edges from one parent - e.g. several calls of the same method - become one node.
     */
    private static Node tree(Flow flow) {
        Map<Step, Node> nodes = new IdentityHashMap<>();
        Node root = new Node(flow.start());
        nodes.put(flow.start(), root);
        for (Step step : flow.steps()) {
            if (step == flow.start()) {
                continue;
            }
            Node parent = nodes.get(step.from().orElseThrow());
            Optional<Node> same = parent.children.stream()
                .filter(child -> child.step.kind() == step.kind() && child.step.nodeKey().equals(step.nodeKey()))
                .findFirst();
            if (same.isPresent()) {
                same.get().times++;
                nodes.put(step, same.get());
            } else {
                Node node = new Node(step);
                parent.children.add(node);
                nodes.put(step, node);
            }
        }
        return root;
    }

    private void indexExpansions(Node root, boolean backward) {
        List<Node> pending = new ArrayList<>(List.of(root));
        while (!pending.isEmpty()) {
            Node node = pending.remove(pending.size() - 1);
            if (!node.isLeaf()) {
                expansions.putIfAbsent(directionKey(node, backward), node);
            }
            pending.addAll(node.children);
        }
    }

    private static List<Node> distinct(List<Node> roots) {
        Map<String, Node> byKey = new LinkedHashMap<>();
        roots.forEach(root -> byKey.putIfAbsent(root.step.nodeKey(), root));
        return List.copyOf(byKey.values());
    }

    private boolean isAccessor(Node node) {
        if (!node.isLeaf() || !(node.step instanceof Step.MethodStep methodStep)) {
            return false;
        }
        MethodMirror method = methodStep.method().mirror();
        boolean withoutParameters = method.getParameters().isEmpty();
        return domainMirror.getDomainTypeMirror(methodStep.method().typeName())
            .map(type -> switch (type.getDomainType()) {
                case ENUM -> true;
                case VALUE_OBJECT, IDENTITY, DOMAIN_COMMAND, DOMAIN_EVENT, READ_MODEL -> withoutParameters;
                case ENTITY, AGGREGATE_ROOT, NON_DOMAIN -> withoutParameters && isGetter(type, method.getName());
                default -> false;
            })
            .orElse(false);
    }

    private static boolean isGetter(DomainTypeMirror type, String methodName) {
        return methodName.startsWith("get") || methodName.startsWith("is") || methodName.startsWith("has")
            // record style: named like the field it reads
            || type.getAllFields().stream().anyMatch(field -> field.getName().equals(methodName));
    }

    // ---------------------------------------------------------------------
    // Laying out
    // ---------------------------------------------------------------------

    /**
     * One layout of the flows. Run twice: a dry run finds the nodes shown more than once, which the second run numbers
     * where they are expanded and refers to everywhere else.
     */
    private final class Rendering {

        private final Options options;
        private final Map<String, Integer> referenceIds;
        private final List<Line> lines = new ArrayList<>();
        /** per direction: a node's backward and forward expansions differ */
        private final Set<String> expanded = new HashSet<>();
        private final List<String> expansionOrder = new ArrayList<>();
        private final Map<String, Integer> occurrences = new HashMap<>();
        private final Map<Node, Boolean> matches = new IdentityHashMap<>();

        private Rendering(Options options, Map<String, Integer> referenceIds) {
            this.options = options;
            this.referenceIds = referenceIds;
        }

        private void render() {
            unresolved.forEach(point -> lines.add(new Line(LineKind.NOTE, "Not found in the domain model: " + point, false)));
            if (!backwardRoots.isEmpty()) {
                renderBackward();
            }
            if (!forwardRoots.isEmpty()) {
                renderForward();
            }
            if (lines.stream().noneMatch(line -> line.kind() == LineKind.STEP)) {
                lines.add(new Line(LineKind.NOTE, options.searching() ? "No step contains \"" + options.search().trim() + "\"."
                    : "No flow to show.", false));
            }
        }

        private void renderBackward() {
            int heading = lines.size();
            List<String> withoutPredecessors = new ArrayList<>();
            for (Node root : backwardRoots) {
                if (options.searching() && !matches(root, true)) {
                    continue;
                }
                expanded.add(directionKey(root, true));
                List<Line> block = new ArrayList<>();
                children(root, "", 0, true, block);
                if (block.isEmpty()) {
                    withoutPredecessors.add(label(root));
                    continue;
                }
                block.add(0, stepLine(label(root) + "   ◀ target", root));
                Collections.reverse(block);
                block.forEach(line -> lines.add(new Line(line.kind(), upsideDown(line.text()), line.match())));
                lines.add(new Line(LineKind.BLANK, "", false));
            }
            if (!withoutPredecessors.isEmpty()) {
                lines.add(new Line(LineKind.NOTE, "Nothing found leading into:", false));
                withoutPredecessors.forEach(label -> lines.add(new Line(LineKind.STEP, "   " + label + "   ◀ target",
                    containsSearch(label))));
                lines.add(new Line(LineKind.BLANK, "", false));
            }
            if (lines.size() > heading) {
                lines.add(heading, new Line(LineKind.HEADING, "▲ WHAT LEADS INTO IT", false));
            }
        }

        private void renderForward() {
            int heading = lines.size();
            for (Node root : forwardRoots) {
                if (options.searching() && !matches(root, false)) {
                    continue;
                }
                expanded.add(directionKey(root, false));
                lines.add(stepLine(label(root) + "   ◀ start", root));
                children(root, "", 0, false, lines);
                lines.add(new Line(LineKind.BLANK, "", false));
            }
            if (lines.size() > heading) {
                lines.add(heading, new Line(LineKind.HEADING, "▼ WHAT IT LEADS TO", false));
            }
        }

        private void children(Node node, String indent, int depth, boolean backward, List<Line> out) {
            List<Node> visible = new ArrayList<>();
            int hiddenAccessors = 0;
            for (Node child : node.children) {
                if (backward && startKeys.contains(child.step.nodeKey())) {
                    // in the backward part the start command precedes each method processing it: noise, it is the start
                    continue;
                }
                if (options.hideAccessors() && isAccessor(child) && !(options.searching() && matches(child, backward))) {
                    hiddenAccessors++;
                } else if (options.hideAccessors() && backward && child.step instanceof Step.CommandStep && child.isLeaf()) {
                    // the method processing a command names it as parameter already
                    continue;
                } else if (!options.searching() || matches(child, backward)) {
                    visible.add(child);
                }
            }
            if (options.searching()) {
                hiddenAccessors = 0;
            }

            for (int i = 0; i < visible.size(); i++) {
                Node child = visible.get(i);
                boolean last = i == visible.size() - 1 && hiddenAccessors == 0;
                String key = directionKey(child, backward);
                boolean startReference = startKeys.contains(child.step.nodeKey());
                boolean expandable = !child.step.cyclic() && !startReference
                    && (!child.isLeaf() || expansions.containsKey(key));
                boolean cutByDepth = expandable && options.maxDepth() > 0 && depth + 1 >= options.maxDepth();
                boolean expand = expandable && !cutByDepth && !expanded.contains(key);
                boolean reference = expandable && !cutByDepth && !expand;
                if (expand) {
                    expanded.add(key);
                    expansionOrder.add(key);
                }
                if (expand || reference) {
                    occurrences.merge(key, 1, Integer::sum);
                }

                StringBuilder text = new StringBuilder(indent)
                    .append(last ? "└" : "├")
                    .append(child.step.kind() == StepKind.IMPLEMENTATION ? "⇒ " : "─ ")
                    .append(label(child));
                Integer id = referenceIds.get(key);
                if (child.step.cyclic()) {
                    text.append("   ↻ cycle");
                } else if (startReference) {
                    text.append("   → see start");
                } else if (expand && id != null) {
                    text.append("   [").append(id).append(']');
                } else if (reference && id != null) {
                    text.append("   → see [").append(id).append(']');
                } else if (cutByDepth) {
                    text.append("   …");
                }
                if (child.times > 1 && child.step.kind() == StepKind.CALL) {
                    text.append("   ×").append(child.times);
                }
                out.add(stepLine(text.toString(), child));
                if (expand) {
                    // a step may be expanded in another flow than the one this occurrence belongs to
                    Node expansion = child.isLeaf() ? expansions.get(key) : child;
                    children(expansion, indent + (last ? "   " : "│  "), depth + 1, backward, out);
                }
            }
            if (hiddenAccessors > 0) {
                out.add(new Line(LineKind.NOTE, indent + "└─ … " + hiddenAccessors
                    + (hiddenAccessors == 1 ? " accessor hidden" : " accessors hidden"), false));
            }
        }

        private Line stepLine(String text, Node node) {
            return new Line(LineKind.STEP, text, options.searching() && containsSearch(label(node)));
        }

        /**
         * Whether the node or any node below it contains the searched text - the paths shown while searching. Below a
         * node referring to another occurrence of its step lies that occurrence's expansion.
         */
        private boolean matches(Node node, boolean backward) {
            Boolean known = matches.get(node);
            if (known != null) {
                return known;
            }
            // guards against cycles through references
            matches.put(node, false);
            Node expansion = node.isLeaf() ? expansions.get(directionKey(node, backward)) : null;
            boolean match = containsSearch(label(node))
                || node.children.stream().anyMatch(child -> !child.step.cyclic() && matches(child, backward))
                || expansion != null && !node.step.cyclic() && matches(expansion, backward);
            matches.put(node, match);
            return match;
        }

        private boolean containsSearch(String text) {
            return options.searching()
                && text.toLowerCase(Locale.ROOT).contains(options.search().trim().toLowerCase(Locale.ROOT));
        }
    }

    private static String directionKey(Node node, boolean backward) {
        return (backward ? "B:" : "F:") + node.step.nodeKey();
    }

    /**
     * Turns a line of a tree upside down: its branches open upwards.
     */
    private static String upsideDown(String line) {
        int prefixEnd = 0;
        while (prefixEnd < line.length() && "│├└─⇒ ".indexOf(line.charAt(prefixEnd)) >= 0) {
            prefixEnd++;
        }
        return line.substring(0, prefixEnd).replace('└', '┌') + line.substring(prefixEnd);
    }

    // ---------------------------------------------------------------------
    // Labels
    // ---------------------------------------------------------------------

    private String label(Node node) {
        Step step = node.step;
        String label;
        if (step instanceof Step.MethodStep methodStep) {
            MethodMirror method = methodStep.method().mirror();
            label = simpleName(methodStep.method().typeName()) + "." + method.getName() + "("
                + method.getParameters().stream()
                    .map(parameter -> simpleName(parameter.getType().getTypeName()))
                    .collect(Collectors.joining(", "))
                + ")";
        } else if (step instanceof Step.TypeStep typeStep) {
            label = "[" + domainTypeLabel(typeStep.type().getDomainType()) + "] " + simpleName(step.typeName());
        } else if (step instanceof Step.CommandStep) {
            label = "[Command] " + simpleName(step.typeName());
        } else if (step instanceof Step.EventStep) {
            label = "[Event] " + simpleName(step.typeName());
        } else {
            label = step.describe();
        }
        String context = context(step.typeName());
        return context != null && !context.equals(startContext) ? label + "   ⟨" + context + "⟩" : label;
    }

    private static String domainTypeLabel(DomainType domainType) {
        return switch (domainType) {
            case AGGREGATE_ROOT -> "Aggregate";
            case READ_MODEL -> "ReadModel";
            default -> Arrays.stream(domainType.name().split("_"))
                .map(word -> word.charAt(0) + word.substring(1).toLowerCase(Locale.ROOT))
                .collect(Collectors.joining());
        };
    }

    private static String simpleName(String typeName) {
        String simpleName = typeName.substring(typeName.lastIndexOf('.') + 1);
        return simpleName.substring(simpleName.lastIndexOf('$') + 1);
    }

    /**
     * The Bounded Context of a type, shown where a flow leaves the one it starts in. A type outside of all Bounded
     * Contexts is named by its package below the one the Bounded Contexts share, e.g. "shared".
     */
    private String context(String typeName) {
        for (BoundedContextMirror boundedContext : domainMirror.getAllBoundedContextMirrors()) {
            if (typeName.startsWith(boundedContext.getPackageName() + ".")) {
                return boundedContext.getName().orElse(simpleName(boundedContext.getPackageName()));
            }
        }
        if (commonContextPackage == null || !typeName.startsWith(commonContextPackage + ".")) {
            return null;
        }
        String below = typeName.substring(commonContextPackage.length() + 1);
        int separator = below.indexOf('.');
        return separator < 0 ? null : below.substring(0, separator);
    }

    /**
     * The package the Bounded Contexts are placed in, e.g. {@code com.example} for {@code com.example.orders} and
     * {@code com.example.billing}.
     */
    private static String commonContextPackage(List<BoundedContextMirror> boundedContexts) {
        if (boundedContexts == null || boundedContexts.isEmpty()) {
            return null;
        }
        List<String> common = null;
        for (BoundedContextMirror boundedContext : boundedContexts) {
            List<String> segments = List.of(boundedContext.getPackageName().split("\\."));
            List<String> parent = segments.subList(0, Math.max(0, segments.size() - 1));
            if (common == null) {
                common = parent;
            } else {
                int length = 0;
                while (length < common.size() && length < parent.size() && common.get(length).equals(parent.get(length))) {
                    length++;
                }
                common = common.subList(0, length);
            }
        }
        return common.isEmpty() ? null : String.join(".", common);
    }
}
