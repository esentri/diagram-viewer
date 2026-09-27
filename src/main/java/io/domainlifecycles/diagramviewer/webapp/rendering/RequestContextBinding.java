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

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Carries the Spring request attributes of the request starting a background rendering over to the code that later
 * updates the UI via {@code UI.access}. Vaadin often runs such a command right away on the calling thread - here the
 * rendering thread, which has no request bound - and the UI components read the session scoped
 * {@code SessionStorage}, which Spring resolves via the bound request attributes.
 * <p>
 * The captured attributes keep a reference to the HTTP session, which Spring's {@code ServletRequestAttributes} use
 * for session scoped beans once the original request is completed.
 */
final class RequestContextBinding {

    private final RequestAttributes requestAttributes;

    private RequestContextBinding(RequestAttributes requestAttributes) {
        this.requestAttributes = requestAttributes;
    }

    /**
     * Captures the request attributes bound to the current thread, if any.
     *
     * @return the binding to apply later
     */
    static RequestContextBinding capture() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            // makes the attributes remember the session while the request is still active
            requestAttributes.getSessionMutex();
        }
        return new RequestContextBinding(requestAttributes);
    }

    /**
     * Runs the command with the captured request attributes bound, unless the running thread has request attributes
     * of its own (e.g. when Vaadin runs the command within a later request of the same session).
     *
     * @param command the command to run
     */
    void run(Runnable command) {
        boolean bind = requestAttributes != null && RequestContextHolder.getRequestAttributes() == null;
        if (bind) {
            RequestContextHolder.setRequestAttributes(requestAttributes);
        }
        try {
            command.run();
        } finally {
            if (bind) {
                RequestContextHolder.resetRequestAttributes();
            }
        }
    }
}
