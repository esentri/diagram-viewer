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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.SessionScope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The UI update after a background rendering may run on the rendering thread, where the session scoped
 * {@code SessionStorage} must still resolve to the user's instance (regression: ScopeNotActiveException).
 */
class RequestContextBindingTest {

    private static final String BEAN_NAME = "scopedTarget.sessionStorage";

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void Should_ResolveSessionScopedBeanOfTheUser_When_RunOnAnotherThreadAfterTheRequestCompleted() throws Exception {

        // given: a UI request of a user whose session holds the session scoped bean
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(requestAttributes);
        Object usersBean = new SessionScope().get(BEAN_NAME, Object::new);
        RequestContextBinding binding = RequestContextBinding.capture();
        requestAttributes.requestCompleted();
        RequestContextHolder.resetRequestAttributes();

        // when: the UI update runs on the rendering thread
        Object resolved = CompletableFuture.supplyAsync(() -> {
            Object[] result = new Object[1];
            binding.run(() -> result[0] = new SessionScope().get(BEAN_NAME, Object::new));
            return result[0];
        }).get(10, TimeUnit.SECONDS);

        // then: it is the user's instance, not a new one or an error
        assertThat(resolved).isSameAs(usersBean);
    }

    @Test
    void Should_LeaveNoRequestAttributesBehind_When_RunOnAThreadWithoutOwnAttributes() throws Exception {

        // given
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        RequestContextBinding binding = RequestContextBinding.capture();
        RequestContextHolder.resetRequestAttributes();

        // when / then: the pooled rendering thread is clean again afterwards
        boolean clean = CompletableFuture.supplyAsync(() -> {
            binding.run(() -> { });
            return RequestContextHolder.getRequestAttributes() == null;
        }).get(10, TimeUnit.SECONDS);
        assertThat(clean).isTrue();
    }

    @Test
    void Should_FailLikeBefore_When_NoRequestAttributesWereCaptured() {
        RequestContextBinding binding = RequestContextBinding.capture();

        assertThatThrownBy(() -> binding.run(() -> new SessionScope().get(BEAN_NAME, Object::new)))
            .isInstanceOf(IllegalStateException.class);
    }
}
