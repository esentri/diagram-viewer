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

package io.domainlifecycles.diagramviewer.scheduled;

import io.domainlifecycles.diagramviewer.exception.DiagramRegenerationTaskException;
import io.domainlifecycles.diagramviewer.model.task.RegenerateDiagramsJob;
import io.domainlifecycles.diagramviewer.service.DiagramRegenerationService;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.RegenerateDiagramsJobService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DiagramRegenerationTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiagramRegenerationTask.class);

    private final RegenerateDiagramsJobService regenerateDiagramsJobService;
    private final DiagramRegenerationService diagramRegenerationService;
    private final DiagramService diagramService;

    public DiagramRegenerationTask(
            RegenerateDiagramsJobService regenerateDiagramsJobService,
            DiagramRegenerationService diagramRegenerationService,
            DiagramService diagramService
    ) {
        this.regenerateDiagramsJobService = regenerateDiagramsJobService;
        this.diagramRegenerationService = diagramRegenerationService;
        this.diagramService = diagramService;
    }

    @Scheduled(fixedRateString = "${regenerateDiagramsTask.rate}")
    public void regenerateUpdatedDomainMirrors() {
        List<DiagramRegenerationError> caughtErrors = new ArrayList<>();
        List<RegenerateDiagramsJob> allJobs = regenerateDiagramsJobService.getAll();
        LOGGER.debug("Found {} diagrams to regenerate after DomainMirror update.", allJobs.size());

        Map<UUID, List<RegenerateDiagramsJob>> jobsGroupedByProjectId = allJobs.stream()
            .collect(Collectors.groupingBy(job -> job.getDiagram().getProject().getId()));

        jobsGroupedByProjectId.forEach((projectId, value) -> {
            LOGGER.info("Regenerating diagrams for project '{}' ...", projectId);

            List<RegenerateDiagramsJob> regenerateDiagramsJobsForProject = jobsGroupedByProjectId.get(projectId);

            regenerateDiagramsJobsForProject.forEach(job -> {
                try {
                    var diagram = job.getDiagram();
                    diagram.setChangedAt(Instant.now());
                    diagramService.updateModel(diagram);
                    diagramRegenerationService.regenerate(job.getDiagram());
                    regenerateDiagramsJobService.delete(job);
                } catch(Exception e) {
                    LOGGER.error("Error occurred while regenerating diagram '{}'. Continuing with others...",
                        job.getDiagram().getName());

                    caughtErrors.add(DiagramRegenerationError.builder()
                            .diagramId(job.getDiagram().getId())
                            .diagramName(job.getDiagram().getName())
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
