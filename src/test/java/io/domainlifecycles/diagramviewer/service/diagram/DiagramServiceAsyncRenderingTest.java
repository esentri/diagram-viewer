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

package io.domainlifecycles.diagramviewer.service.diagram;

import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.repository.DiagramTypeNoteRepository;
import io.domainlifecycles.diagramviewer.rest.kroki.KrokiClient;
import io.domainlifecycles.diagramviewer.scenario.RezeptionScenario;
import io.domainlifecycles.diagramviewer.service.DiagramRendering;
import io.domainlifecycles.diagramviewer.service.DiagramServiceImpl;
import io.domainlifecycles.diagramviewer.service.ProjectModel;
import io.domainlifecycles.diagramviewer.service.ProjectModelCache;
import io.domainlifecycles.diagramviewer.util.DiagramFileUtils;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.serialize.jackson3.JacksonDomainSerializer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Background rendering of diagrams: superseded renderings must neither overwrite a newer
 * image nor report failures. Uses the real rezeption domain model, since the diagram generation runs on the rendering
 * thread, where static mocks do not apply.
 */
class DiagramServiceAsyncRenderingTest {

    private static DomainMirror domainMirror;

    @TempDir
    Path diagramsLocation;

    private final KrokiClient krokiClient = mock(KrokiClient.class);
    private final UUID projectId = UUID.randomUUID();
    private DiagramServiceImpl diagramService;
    private Diagram diagram;

    @BeforeAll
    static void loadDomainMirror() {
        domainMirror = new JacksonDomainSerializer(false).deserialize(RezeptionScenario.domainMirrorJson());
    }

    @BeforeEach
    void setUp() {
        diagramService = service(1000);
        diagram = Diagram.builder()
            .id(UUID.randomUUID())
            .name("async")
            .project(Project.builder().id(projectId).build())
            .build();
    }

    @AfterEach
    void tearDown() {
        diagramService.shutdownRenderingExecutor();
    }

    private DiagramServiceImpl service(int largeDiagramClasses) {
        ProjectModelCache cache = mock(ProjectModelCache.class);
        when(cache.get(any())).thenReturn(
            new ProjectModel(Instant.now(), domainMirror, List.of(), List.of(), false, Optional::empty));
        DiagramRepository repository = mock(DiagramRepository.class);
        when(repository.findByProjectIdAndName(any(), any())).thenReturn(List.of());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        return new DiagramServiceImpl(diagramsLocation.toString(), cache, repository,
            mock(DiagramTypeNoteRepository.class), krokiClient, 1, largeDiagramClasses, 1024);
    }

    @Test
    void Should_SaveOnlyTheLatestImage_When_RenderingsOfTheSameDiagramOverlap() throws Exception {

        // given: the first conversion blocks until released
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        AtomicInteger conversions = new AtomicInteger();
        when(krokiClient.convert(any())).thenAnswer(invocation -> {
            if (conversions.incrementAndGet() == 1) {
                firstStarted.countDown();
                releaseFirst.await(10, TimeUnit.SECONDS);
                return "first".getBytes(StandardCharsets.UTF_8);
            }
            return "latest".getBytes(StandardCharsets.UTF_8);
        });

        // when: two more renderings are requested while the first one is converting
        DiagramRendering first = diagramService.updateModelAndImageAsync(diagram);
        assertThat(firstStarted.await(10, TimeUnit.SECONDS)).isTrue();
        DiagramRendering second = diagramService.updateModelAndImageAsync(diagram);
        DiagramRendering third = diagramService.updateModelAndImageAsync(diagram);
        releaseFirst.countDown();

        // then: the first drops its result, the second is skipped before converting, the latest is saved
        assertThat(result(first).saved()).isFalse();
        assertThat(result(second).saved()).isFalse();
        assertThat(result(third).saved()).isTrue();
        assertThat(conversions.get()).isEqualTo(2);
        assertThat(Files.readString(svgPath())).isEqualTo("latest");
    }

    @Test
    void Should_ReportFailure_When_LatestRenderingFails() {

        // given
        when(krokiClient.convert(any())).thenThrow(new IllegalStateException("kroki down"));

        // when
        DiagramRendering rendering = diagramService.updateModelAndImageAsync(diagram);

        // then
        assertThatThrownBy(() -> result(rendering))
            .isInstanceOf(ExecutionException.class)
            .hasRootCauseMessage("kroki down");
    }

    @Test
    void Should_FlagDiagramAsLarge_When_ItExceedsTheConfiguredNumberOfClasses() throws Exception {

        // given: every diagram with more than one class counts as large
        diagramService.shutdownRenderingExecutor();
        diagramService = service(1);
        when(krokiClient.convert(any())).thenReturn("svg".getBytes(StandardCharsets.UTF_8));

        // when
        DiagramRendering.Result result = result(diagramService.updateModelAndImageAsync(diagram));

        // then
        assertThat(result.saved()).isTrue();
        assertThat(result.classCount()).isGreaterThan(1);
        assertThat(result.large()).isTrue();
    }

    private static DiagramRendering.Result result(DiagramRendering rendering) throws Exception {
        return rendering.image().get(30, TimeUnit.SECONDS);
    }

    private Path svgPath() {
        return DiagramFileUtils.imagePath(diagramsLocation.toString(), diagram);
    }
}
