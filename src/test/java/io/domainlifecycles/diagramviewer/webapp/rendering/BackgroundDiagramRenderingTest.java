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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.Command;
import io.domainlifecycles.diagramviewer.service.DiagramRendering;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class BackgroundDiagramRenderingTest {

    @Test
    void Should_ReportFailedRenderings_When_AllAreDone() throws Exception {

        // given: a UI running access commands right away, and two pending renderings
        UI ui = mock(UI.class);
        doAnswer(invocation -> {
            ((Command) invocation.getArgument(0)).execute();
            return null;
        }).when(ui).access(any());
        CompletableFuture<DiagramRendering.Result> succeeding = new CompletableFuture<>();
        CompletableFuture<DiagramRendering.Result> failing = new CompletableFuture<>();
        AtomicInteger reportedFailures = new AtomicInteger(-1);
        CountDownLatch done = new CountDownLatch(1);

        BackgroundDiagramRendering.whenAllRendered(ui, List.of(succeeding, failing), failed -> {
            reportedFailures.set(failed);
            done.countDown();
        });

        // when: one rendering finishes - nothing reported yet
        succeeding.complete(new DiagramRendering.Result(true, 3, false));
        assertThat(done.getCount()).isEqualTo(1);

        // when: the other one fails
        failing.completeExceptionally(new IllegalStateException("Kroki down"));

        // then
        assertThat(done.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(reportedFailures.get()).isEqualTo(1);
    }
}
