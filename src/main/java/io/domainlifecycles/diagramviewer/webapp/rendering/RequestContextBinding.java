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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Carries the thread bound context of the request starting background work over to the code that later updates the
 * UI via {@code UI.access}. Vaadin often runs such a command right away on the calling thread - a rendering or
 * analysis thread, which has no request bound - while the UI components need what the request had:
 * <ul>
 *     <li>the Spring request attributes, to resolve the session scoped {@code SessionStorage}. The captured attributes
 *     keep a reference to the HTTP session, which Spring's {@code ServletRequestAttributes} use for session scoped
 *     beans once the original request is completed;</li>
 *     <li>the Spring Security context, to know the signed in user (e.g. for the navigation listing their projects).</li>
 * </ul>
 */
final class RequestContextBinding {

    private final RequestAttributes requestAttributes;
    private final SecurityContext securityContext;

    private RequestContextBinding(RequestAttributes requestAttributes, SecurityContext securityContext) {
        this.requestAttributes = requestAttributes;
        this.securityContext = securityContext;
    }

    /**
     * Captures the request attributes and the security context bound to the current thread, if any.
     *
     * @return the binding to apply later
     */
    static RequestContextBinding capture() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            // makes the attributes remember the session while the request is still active
            requestAttributes.getSessionMutex();
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityContext securityContext = null;
        if (authentication != null) {
            securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
        }
        return new RequestContextBinding(requestAttributes, securityContext);
    }

    /**
     * Runs the command with the captured request attributes and security context bound - each unless the running
     * thread has one of its own (e.g. when Vaadin runs the command within a later request of the same session).
     *
     * @param command the command to run
     */
    void run(Runnable command) {
        boolean bindRequest = requestAttributes != null && RequestContextHolder.getRequestAttributes() == null;
        boolean bindSecurity = securityContext != null && SecurityContextHolder.getContext().getAuthentication() == null;
        if (bindRequest) {
            RequestContextHolder.setRequestAttributes(requestAttributes);
        }
        if (bindSecurity) {
            SecurityContextHolder.setContext(securityContext);
        }
        try {
            command.run();
        } finally {
            if (bindSecurity) {
                SecurityContextHolder.clearContext();
            }
            if (bindRequest) {
                RequestContextHolder.resetRequestAttributes();
            }
        }
    }
}
