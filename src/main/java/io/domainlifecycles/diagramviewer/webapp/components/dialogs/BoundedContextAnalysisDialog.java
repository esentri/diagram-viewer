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
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;

/**
 * Shows the progress of "Analyze Bounded Contexts": first the diagrams being created, then their images being rendered
 * in the background. Closing it does not stop anything - the analysis continues in the background.
 */
public final class BoundedContextAnalysisDialog extends Dialog {

    private final ProgressBar progressBar = new ProgressBar();
    private final Span status = new Span("Preparing ...");
    private final Span detail = new Span();
    private final Button closeButton = new Button("Continue in background", e -> close());

    public BoundedContextAnalysisDialog() {
        setHeaderTitle("Analyze Bounded Contexts");
        setWidth("32rem");
        setCloseOnOutsideClick(false);
        progressBar.setIndeterminate(true);
        progressBar.setId("bounded-context-analysis-progress");
        detail.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", "var(--lumo-secondary-text-color)");
        VerticalLayout layout = new VerticalLayout(status, progressBar, detail);
        layout.setPadding(false);
        add(layout);
        getFooter().add(closeButton);
    }

    public void diagramCreated(int done, int total, String diagramName) {
        show(String.format("Creating diagrams: %d of %d", done, total), done, total, diagramName);
    }

    public void diagramRendered(int done, int total) {
        show(String.format("Rendering diagrams: %d of %d", done, total), done, total, "");
    }

    public void finished(String summary) {
        progressBar.setIndeterminate(false);
        progressBar.setValue(1);
        status.setText("Done");
        detail.setText(summary);
        closeButton.setText("Close");
    }

    public void failed(String message) {
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
        status.setText("Analysis failed");
        detail.setText(message);
        closeButton.setText("Close");
    }

    private void show(String statusText, int done, int total, String detailText) {
        progressBar.setIndeterminate(false);
        progressBar.setValue(total == 0 ? 1 : (double) done / total);
        status.setText(statusText);
        detail.setText(detailText);
    }
}
