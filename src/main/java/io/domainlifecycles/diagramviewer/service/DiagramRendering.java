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
import java.util.concurrent.CompletableFuture;

/**
 * A diagram whose model has been saved and whose image is rendered in the background.
 *
 * @param diagram the saved diagram
 * @param image   completes when the background rendering is done - with the outcome, or exceptionally if rendering
 *                failed
 */
public record DiagramRendering(Diagram diagram, CompletableFuture<Result> image) {

    /**
     * @param saved      {@code true} if the rendered image was saved; {@code false} if the rendering was superseded
     *                   by a newer one of the same diagram and its result dropped
     * @param classCount the number of classes in the diagram
     * @param large      {@code true} if the diagram exceeds the configured size above which users should
     *                   restrict it with filters
     */
    public record Result(boolean saved, int classCount, boolean large) {

        public static final Result SUPERSEDED = new Result(false, 0, false);
    }
}
