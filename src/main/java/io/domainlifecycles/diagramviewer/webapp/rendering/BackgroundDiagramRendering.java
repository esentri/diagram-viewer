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

package io.domainlifecycles.diagramviewer.webapp.rendering;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.service.DiagramRendering;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramReRenderedEvent;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramRenderingFailedEvent;
import io.domainlifecycles.diagramviewer.webapp.events.DiagramRenderingStartedEvent;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.IntConsumer;
import lombok.extern.slf4j.Slf4j;

/**
 * Connects the background rendering of diagrams to the user interface: the model is saved
 * within the request, the image is rendered in the background, and the UI is informed via {@code UI.access} (server
 * push) - {@link DiagramRenderingStartedEvent} right away, then {@link DiagramReRenderedEvent} or
 * {@link DiagramRenderingFailedEvent}. Superseded renderings stay silent, the latest one reports.
 */
@Slf4j
public final class BackgroundDiagramRendering {

    private static final int NOTIFICATION_DURATION_MS = 8000;

    private BackgroundDiagramRendering() {
    }

    /**
     * Saves the diagram's model and renders its image in the background, see
     * {@link DiagramService#updateModelAndImageAsync(Diagram)}.
     *
     * @param source         the component triggering the rendering, the source of the fired events
     * @param diagramService the service rendering the diagram
     * @param diagram        the diagram to save and render
     * @return the saved diagram
     */
    public static Diagram updateModelAndImage(Component source, DiagramService diagramService, Diagram diagram) {
        UI ui = UI.getCurrent();
        DiagramRendering rendering = diagramService.updateModelAndImageAsync(diagram);
        follow(source, ui, rendering);
        return rendering.diagram();
    }

    /**
     * Informs the UI about the progress of a rendering started in the background.
     *
     * @param source    the component triggering the rendering, the source of the fired events
     * @param ui        the UI to inform
     * @param rendering the rendering to follow
     */
    public static void follow(Component source, UI ui, DiagramRendering rendering) {
        String diagramName = rendering.diagram().getName();
        RequestContextBinding requestContext = RequestContextBinding.capture();
        if (ui != null) {
            ComponentUtil.fireEvent(ui, new DiagramRenderingStartedEvent(source, false));
        }
        rendering.image().whenComplete((result, error) -> {
            if (error != null) {
                Throwable cause = error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
                log.error("Rendering diagram '{}' failed.", diagramName, cause);
                access(ui, requestContext, () -> {
                    ComponentUtil.fireEvent(ui, new DiagramRenderingFailedEvent(source, false));
                    notify(String.format("Rendering diagram '%s' failed: %s", diagramName, cause.getMessage()),
                        NotificationVariant.LUMO_ERROR);
                });
            } else if (result.saved()) {
                access(ui, requestContext, () -> {
                    ComponentUtil.fireEvent(ui, new DiagramReRenderedEvent(source, false));
                    if (result.large()) {
                        notify(String.format("Diagram '%s' contains %d classes. Restrict it with package or flow"
                                + " filters to keep it readable and fast.", diagramName, result.classCount()),
                            NotificationVariant.LUMO_CONTRAST);
                    }
                });
            }
        });
    }

    /**
     * Runs a command on the UI once all given background renderings are done, e.g. to refresh the diagram cards
     * after many diagrams were created at once.
     *
     * @param ui         the UI to update
     * @param renderings the renderings to wait for
     * @param onDone     receives the number of failed renderings; runs with the UI locked
     */
    public static void whenAllRendered(UI ui, List<CompletableFuture<DiagramRendering.Result>> renderings,
                                       IntConsumer onDone) {
        RequestContextBinding requestContext = RequestContextBinding.capture();
        CompletableFuture.allOf(renderings.stream()
                .map(rendering -> rendering.handle((result, error) -> error))
                .toArray(CompletableFuture[]::new))
            .thenRun(() -> {
                int failed = (int) renderings.stream().filter(CompletableFuture::isCompletedExceptionally).count();
                if (failed > 0) {
                    log.warn("{} of {} diagram renderings failed.", failed, renderings.size());
                }
                access(ui, requestContext, () -> onDone.accept(failed));
            });
    }

    private static void notify(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, NOTIFICATION_DURATION_MS, Notification.Position.BOTTOM_END);
        notification.addThemeVariants(variant);
    }

    private static void access(UI ui, RequestContextBinding requestContext, Runnable command) {
        if (ui == null) {
            return;
        }
        try {
            ui.access(() -> requestContext.run(command));
        } catch (UIDetachedException e) {
            log.debug("UI detached before the diagram rendering finished.");
        }
    }
}
