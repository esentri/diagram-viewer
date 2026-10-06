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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import io.domainlifecycles.diagramviewer.util.FlowText;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.staticanalysis.DomainCalls;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

/**
 * Shows the flows a diagram is restricted to as text, see {@link FlowText}. Large flows can be narrowed down: by the
 * number of steps shown from a start or target, by hiding the accessors, and by a search showing only the paths to the
 * steps containing it.
 */
public final class FlowTextDialog extends Dialog {

    static final int ALL_STEPS = 0;
    /** up to this number of lines a flow is shown completely at first, larger ones only to {@link #DEFAULT_DEPTH} */
    private static final int COMPLETE_UP_TO_LINES = 300;
    private static final int DEFAULT_DEPTH = 5;

    private static final String LEGEND = "Read from top to bottom, in call order.   "
        + "─ call   ⇒ implementation   [n] expanded here, → see [n] elsewhere   ↻ cycle   "
        + "… more steps (raise the depth)   ×n call sites   ⟨…⟩ other Bounded Context";

    private final FlowText flowText;
    private final String diagramName;
    private final Collection<String> flowsFrom;
    private final Collection<String> flowsTo;

    private final Select<Integer> depthSelect = new Select<>();
    private final Checkbox hideAccessorsCheckbox = new Checkbox("Hide accessors", true);
    private final TextField searchField = new TextField();
    private final Div content = new Div();
    private final Span lineCount = new Span();
    private List<FlowText.Line> lines = List.of();

    public FlowTextDialog(DomainMirror domainMirror, DomainCalls domainCalls, String diagramName,
                          Collection<String> flowsFrom, Collection<String> flowsTo) {
        this.flowText = new FlowText(domainMirror, domainCalls, flowsFrom, flowsTo);
        this.diagramName = diagramName;
        this.flowsFrom = flowsFrom;
        this.flowsTo = flowsTo;

        setHeaderTitle("Flow: " + diagramName);
        setWidth("90vw");
        setHeight("85vh");
        setResizable(true);
        setDraggable(true);
        Button closeButton = new Button(VaadinIcon.CLOSE.create(), e -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.setAriaLabel("Close");
        getHeader().add(closeButton);

        depthSelect.setLabel("Depth");
        depthSelect.setItems(3, 5, 10, ALL_STEPS);
        depthSelect.setItemLabelGenerator(depth -> depth == ALL_STEPS ? "All" : depth + " steps");
        depthSelect.setWidth("9rem");

        searchField.setLabel("Search");
        searchField.setPlaceholder("Class or method");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setWidth("20rem");
        searchField.setHelperText("Shows only the paths leading to it");

        Button copyButton = new Button("Copy", VaadinIcon.COPY_O.create(), e -> copyToClipboard());
        Anchor downloadAnchor = new Anchor(downloadHandler(), "");
        downloadAnchor.getElement().setAttribute("download", true);
        downloadAnchor.add(new Button("Download", VaadinIcon.DOWNLOAD_ALT.create()));

        HorizontalLayout toolbar = new HorizontalLayout(depthSelect, hideAccessorsCheckbox, searchField, copyButton,
            downloadAnchor);
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
        toolbar.setWidthFull();
        toolbar.getStyle().set("flex-wrap", "wrap");

        Span legend = new Span(LEGEND);
        legend.getStyle().set("font-size", "var(--lumo-font-size-xs)").set("color", "var(--lumo-secondary-text-color)");
        lineCount.getStyle().set("font-size", "var(--lumo-font-size-xs)").set("color", "var(--lumo-secondary-text-color)");

        content.setId("flow-text");
        content.setWidthFull();
        content.getStyle()
            .set("flex-grow", "1")
            .set("overflow", "auto")
            .set("font-family", "var(--lumo-font-family-monospace, monospace)")
            .set("font-size", "var(--lumo-font-size-s)")
            .set("line-height", "1.35")
            .set("white-space", "pre")
            .set("border", "1px solid var(--lumo-contrast-10pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("padding", "var(--lumo-space-s)");

        VerticalLayout layout = new VerticalLayout(toolbar, legend, content, lineCount);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setSizeFull();
        layout.getStyle().set("gap", "var(--lumo-space-xs)");
        add(layout);

        depthSelect.setValue(flowText.render(new FlowText.Options(true, ALL_STEPS, null)).size() <= COMPLETE_UP_TO_LINES
            ? ALL_STEPS : DEFAULT_DEPTH);
        depthSelect.addValueChangeListener(e -> refresh());
        hideAccessorsCheckbox.addValueChangeListener(e -> refresh());
        searchField.addValueChangeListener(e -> refresh());
        refresh();
    }

    private void refresh() {
        Integer depth = depthSelect.getValue();
        lines = flowText.render(new FlowText.Options(hideAccessorsCheckbox.getValue(),
            depth == null ? ALL_STEPS : depth, searchField.getValue()));
        content.removeAll();
        for (FlowText.Line line : lines) {
            content.add(lineComponent(line));
        }
        long matches = lines.stream().filter(FlowText.Line::match).count();
        lineCount.setText(lines.size() + " lines" + (searchField.getValue().isBlank() ? "" : ", " + matches + " matches"));
    }

    private static Div lineComponent(FlowText.Line line) {
        // a non-breaking space keeps empty lines from collapsing
        Div div = new Div(line.text().isEmpty() ? " " : line.text());
        switch (line.kind()) {
            case HEADING -> div.getStyle().set("font-weight", "bold").set("margin-top", "var(--lumo-space-xs)");
            case NOTE -> div.getStyle().set("color", "var(--lumo-secondary-text-color)");
            default -> {
                // plain step
            }
        }
        if (line.match()) {
            div.getStyle().set("background", "var(--lumo-primary-color-10pct)").set("font-weight", "600");
        }
        return div;
    }

    /**
     * @return the text as copied or downloaded: what the diagram is restricted to, followed by the lines shown
     */
    String plainText() {
        StringBuilder text = new StringBuilder("Flow of diagram \"").append(diagramName).append("\"\n");
        if (!flowsFrom.isEmpty()) {
            text.append("Forward from: ").append(String.join(", ", flowsFrom)).append('\n');
        }
        if (!flowsTo.isEmpty()) {
            text.append("Backward to: ").append(String.join(", ", flowsTo)).append('\n');
        }
        text.append(LEGEND).append("\n\n");
        return text.append(FlowText.toText(lines)).toString();
    }

    private void copyToClipboard() {
        getElement().executeJs("return navigator.clipboard.writeText($0)", plainText())
            .then(result -> Notification.show("Copied the flow to the clipboard"),
                error -> Notification.show("Copying failed: " + error));
    }

    private DownloadHandler downloadHandler() {
        return DownloadHandler.fromInputStream(event -> {
            byte[] bytes = plainText().getBytes(StandardCharsets.UTF_8);
            return new DownloadResponse(new ByteArrayInputStream(bytes), fileName(), "text/plain;charset=utf-8",
                bytes.length);
        });
    }

    private String fileName() {
        return diagramName.replaceAll("[^A-Za-z0-9._-]+", "_") + "-flow.txt";
    }

    List<FlowText.Line> lines() {
        return lines;
    }

    Select<Integer> depthSelect() {
        return depthSelect;
    }

    Checkbox hideAccessorsCheckbox() {
        return hideAccessorsCheckbox;
    }

    TextField searchField() {
        return searchField;
    }
}
