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

package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.domainlifecycles.diagramviewer.service.BoundedContext;
import io.domainlifecycles.diagramviewer.service.BoundedContextAnalysisService.Kind;
import org.springframework.web.util.HtmlUtils;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Lets the user choose which analyses of the Bounded Contexts are run - or, if run before, completed - before they
 * start. All of them are chosen when the dialog opens - except for the analyses showing flows if the project has no
 * static analysis result: they are disabled, and a hint tells why.
 */
public final class AnalyzeBoundedContextsDialog extends Dialog {

    private final Map<Kind, Checkbox> checkboxes = new EnumMap<>(Kind.class);
    private final Button analyzeButton = new Button("Analyze");

    /** the id of the hint why the analyses showing flows are disabled */
    static final String STATIC_ANALYSIS_HINT_ID = "analysis-static-analysis-hint";

    /**
     * @param boundedContexts          the Bounded Contexts of the project
     * @param staticAnalysisAvailable  whether a static analysis result was uploaded for the project - the analyses
     *                                 showing flows need it
     * @param onAnalyze                runs the chosen analyses
     */
    public AnalyzeBoundedContextsDialog(List<BoundedContext> boundedContexts, boolean staticAnalysisAvailable,
                                        Consumer<Set<Kind>> onAnalyze) {
        setHeaderTitle("Analyze Bounded Contexts");
        setWidth("36rem");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.add(new Html("<div>Creates a folder for each of the " + boundedContexts.size()
            + " Bounded Context(s) of this project ("
            + boundedContexts.stream().map(BoundedContext::label).map(HtmlUtils::htmlEscape)
                .collect(Collectors.joining(", "))
            + ") containing what the chosen analyses create:</div>"));
        for (Kind kind : Kind.values()) {
            boolean available = staticAnalysisAvailable || !kind.needsStaticAnalysis();
            Checkbox checkbox = new Checkbox(kind.getLabel(), available);
            checkbox.setId("analysis-" + kind.name().toLowerCase(Locale.ROOT).replace('_', '-'));
            checkbox.setHelperText(kind.getDescription());
            checkbox.setEnabled(available);
            if (!available) {
                checkbox.setTooltipText("Needs the result of a static analysis");
            }
            checkbox.addValueChangeListener(e -> analyzeButton.setEnabled(!chosenKinds().isEmpty()));
            checkboxes.put(kind, checkbox);
            layout.add(checkbox);
        }
        if (!staticAnalysisAvailable) {
            Span hint = new Span("Read Models and Commands are not available: their diagrams show flows, which are"
                + " known from the result of a static analysis only, and none was uploaded with the domain model of this"
                + " project. Upload the domain model again with the static analysis result (runStaticAnalysis = true in"
                + " the DLC build plugin) to choose them.");
            hint.setId(STATIC_ANALYSIS_HINT_ID);
            hint.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", "var(--lumo-secondary-text-color)");
            layout.add(hint);
        }
        layout.add(new Html("<div>Analyzing again only adds what is missing: existing folders are reused, and diagrams"
            + " whose name already exists in their folder are kept unchanged.</div>"));
        add(layout);

        analyzeButton.setId("analyze-bounded-contexts-confirm");
        analyzeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        analyzeButton.addClickListener(e -> {
            Set<Kind> chosen = chosenKinds();
            close();
            onAnalyze.accept(chosen);
        });
        getFooter().add(new Button("Cancel", e -> close()), analyzeButton);
    }

    /** @return the analyses checked in the dialog */
    public Set<Kind> chosenKinds() {
        Set<Kind> chosen = EnumSet.noneOf(Kind.class);
        checkboxes.forEach((kind, checkbox) -> {
            if (Boolean.TRUE.equals(checkbox.getValue())) {
                chosen.add(kind);
            }
        });
        return chosen;
    }
}
