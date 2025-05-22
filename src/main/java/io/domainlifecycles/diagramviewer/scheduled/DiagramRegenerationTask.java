package io.domainlifecycles.diagramviewer.scheduled;

import io.domainlifecycles.diagramviewer.exception.DiagramRegenerationTaskException;
import io.domainlifecycles.diagramviewer.model.task.RegenerateDiagramsJob;
import io.domainlifecycles.diagramviewer.model.viewer.ProjectDomainMirror;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.ProjectDomainMirrorService;
import io.domainlifecycles.diagramviewer.service.RegenerateDiagramsJobService;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class DiagramRegenerationTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiagramRegenerationTask.class);

    private final RegenerateDiagramsJobService regenerateDiagramsJobService;
    private final ProjectDomainMirrorService projectDomainMirrorService;
    private final DiagramService diagramService;

    public DiagramRegenerationTask(RegenerateDiagramsJobService regenerateDiagramsJobService, ProjectDomainMirrorService projectDomainMirrorService, DiagramService diagramService) {
        this.regenerateDiagramsJobService = regenerateDiagramsJobService;
        this.projectDomainMirrorService = projectDomainMirrorService;
        this.diagramService = diagramService;
    }

    @Scheduled(fixedRateString = "${regenerateDiagramsTask.rate}")
    public void regenerateUpdatedDomainMirrors() {
        List<DiagramRegenerationError> caughtErrors = new ArrayList<>();
        List<RegenerateDiagramsJob> allJobs = regenerateDiagramsJobService.getAll();
        LOGGER.debug(String.format("Found %s diagrams to regenerate after DomainMirror update.", allJobs.size()));

        Map<UUID, List<RegenerateDiagramsJob>> jobsGroupedByProjectId = allJobs.stream()
            .collect(Collectors.groupingBy(job -> job.getDiagram().getProject().getId()));

        jobsGroupedByProjectId.forEach((projectId, value) -> {
            LOGGER.info(String.format("Regenerating diagrams for project '%s' ...", projectId));

            List<RegenerateDiagramsJob> regenerateDiagramsJobsForProject = jobsGroupedByProjectId.get(projectId);
            ProjectDomainMirror projectDomainMirror = projectDomainMirrorService.getByProjectId(projectId);

            regenerateDiagramsJobsForProject.forEach(job -> {
                try {
                    var diagram = job.getDiagram();
                    diagram.setChangedAt(Instant.now());
                    diagramService.update(diagram);
                    diagramService.regenerate(job.getDiagram(), projectDomainMirror.getDomainMirror());
                    regenerateDiagramsJobService.delete(job);
                } catch(Exception e) {
                    LOGGER.error(
                        String.format("Error occurred while regenerating diagram '%s'. Continuing with others...",
                            job.getDiagram().getFileName()));

                    caughtErrors.add(DiagramRegenerationError.builder()
                            .diagramId(job.getDiagram().getId())
                            .diagramName(job.getDiagram().getFileName())
                            .caughtException(e)
                        .build());
                }
            });
        });

        if(!caughtErrors.isEmpty()) {
            throw new DiagramRegenerationTaskException(caughtErrors);
        }

        LOGGER.debug("Diagram regeneration task finished.");
    }

    @Data
    @Builder
    public static class DiagramRegenerationError {
        private final UUID diagramId;
        private final String diagramName;
        private final Throwable caughtException;
    }
}
