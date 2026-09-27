package io.domainlifecycles.diagramviewer.perf;

import io.domainlifecycles.diagramviewer.configuration.BaseIntegrationTest;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.UserStatus;
import io.domainlifecycles.diagramviewer.repository.AppUserRepository;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.service.BoundedContextAnalysisService;
import io.domainlifecycles.diagramviewer.service.DiagramDirectoryService;
import io.domainlifecycles.diagramviewer.service.DiagramRendering;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.DiagramServiceImpl;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.webapp.components.various.cards.DiagramCardGridContainer;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import jakarta.persistence.EntityManagerFactory;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Performance measurement of "Analyze Bounded Contexts" on a large project: uploads a captured upload request body
 * (gzip, {@code -Dperf.upload=...}), analyzes its bounded contexts and waits for all background renderings, printing
 * the heap used over time. Not part of the regular test runs, see {@code ./gradlew perfTest}.
 */
@SpringBootTest(properties = "regenerateDiagramsTask.rate=3600000")
@AutoConfigureMockMvc
@EnabledIfSystemProperty(named = "perf.upload", matches = ".+")
class BoundedContextAnalysis_PerfTest extends BaseIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    DiagramRepository diagramRepository;

    @Autowired
    ProjectService projectService;

    @Autowired
    SessionStorage sessionStorage;

    @Autowired
    BoundedContextAnalysisService boundedContextAnalysisService;

    @Autowired
    DiagramDirectoryService diagramDirectoryService;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    private Project project;

    @AfterEach
    void tearDown() {
        if (project != null) {
            sessionStorage.delete(project.getId());
        }
        diagramRepository.deleteAll();
        projectRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    /** What opening the largest analyzed directory costs on the server - the images are loaded by the browser. */
    private void measureOpeningDirectories(AppUser appUser) {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
        for (int run = 1; run <= 2; run++) {
            statistics.clear();
            long usedBefore = usedHeapMb();
            long start = System.nanoTime();
            Project loaded = projectService.getByName(project.getName());
            long projectMs = (System.nanoTime() - start) / 1_000_000;
            long projectStatements = statistics.getPrepareStatementCount();
            System.out.printf("PERF | load project run %d | %d ms, %d statements, %d entities loaded, %d collections loaded, heap +%d MB%n",
                run, projectMs, projectStatements, statistics.getEntityLoadCount(), statistics.getCollectionLoadCount(),
                usedHeapMb() - usedBefore);
            DiagramDirectory largest = loaded.getDiagramDirectories().stream()
                .max(Comparator.comparingInt(directory -> directory.getDiagrams().size())).orElseThrow();

            statistics.clear();
            start = System.nanoTime();
            DiagramDirectory directory = diagramDirectoryService.getById(largest.getId());
            long directoryMs = (System.nanoTime() - start) / 1_000_000;
            long directoryStatements = statistics.getPrepareStatementCount();

            statistics.clear();
            start = System.nanoTime();
            projectService.getAllAssignedSortedByCreationDate(appUser);
            long navigationMs = (System.nanoTime() - start) / 1_000_000;
            long navigationStatements = statistics.getPrepareStatementCount();

            start = System.nanoTime();
            new DiagramCardGridContainer(diagramDirectoryService, diagramService, loaded, loaded.getSubDirectories(directory), directory.getDiagrams());
            long cardsMs = (System.nanoTime() - start) / 1_000_000;

            System.out.printf("PERF | open directory run %d | project %d ms (%d statements), directory '%s' %d ms (%d statements, %d diagrams),"
                    + " navigation %d ms (%d statements), cards %d ms%n",
                run, projectMs, projectStatements, directory.getName(), directoryMs, directoryStatements,
                directory.getDiagrams().size(), navigationMs, navigationStatements, cardsMs);
        }
    }

    @Test
    void measureBoundedContextAnalysis() throws Exception {
        AppUser appUser = appUserRepository.save(AppUser.builder()
            .firstName("Perf").lastName("Tester").emailAddress("perf-bc-tester@gmail.com")
            .status(UserStatus.ACTIVE).build());
        project = projectRepository.save(Project.builder()
            .name("perf_bounded_contexts").diagrams(new HashSet<>()).diagramDirectories(new HashSet<>())
            .assignedUsers(new HashSet<>(Set.of(appUser))).creator(appUser).build());
        mockMvc.perform(put("/api/upload/domain-mirror/{projectName}", project.getName())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-API-KEY", appUser.getApiKey().toString())
                .header("Content-Encoding", "gzip")
                .content(Files.readAllBytes(Path.of(System.getProperty("perf.upload")))))
            .andExpect(status().isOk());

        AtomicLong maxUsed = new AtomicLong();
        Thread sampler = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                long used = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() >> 20;
                maxUsed.accumulateAndGet(used, Math::max);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }, "heap-sampler");
        sampler.setDaemon(true);
        sampler.start();

        long start = System.nanoTime();
        BoundedContextAnalysisService.Result result = boundedContextAnalysisService.analyze(projectService.getByName(project.getName()));
        long analyzed = (System.nanoTime() - start) / 1_000_000;
        System.out.println("PERF | analyze.ms | " + analyzed + " | created " + result.createdDiagrams());
        if (Boolean.getBoolean("perf.skipRendering")) {
            // only measure what follows: stop the background rendering right away
            ((DiagramServiceImpl) AopTestUtils.getUltimateTargetObject(diagramService)).shutdownRenderingExecutor();
            Thread.sleep(5000);
            System.gc();
            measureOpeningDirectories(appUser);
            return;
        }
        int done = 0;
        int failed = 0;
        for (CompletableFuture<DiagramRendering.Result> rendering : result.renderings()) {
            try {
                rendering.get(10, TimeUnit.MINUTES);
            } catch (Exception e) {
                failed++;
                System.out.println("PERF | rendering failed | " + e.getMessage());
            }
            if (++done % 20 == 0) {
                System.out.println("PERF | rendered | " + done + " after " + (System.nanoTime() - start) / 1_000_000_000
                    + " s, heap used max so far " + maxUsed.get() + " MB");
            }
        }
        sampler.interrupt();
        measureOpeningDirectories(appUser);
        System.out.println("PERF | all rendered.s | " + (System.nanoTime() - start) / 1_000_000_000 + " | failed " + failed
            + " | heap used max " + maxUsed.get() + " MB");
    }

    private static long usedHeapMb() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() >> 20;
    }

    @Autowired
    DiagramService diagramService;
}
