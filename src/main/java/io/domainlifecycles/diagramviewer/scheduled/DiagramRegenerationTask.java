package io.domainlifecycles.diagramviewer.scheduled;

import io.domainlifecycles.diagramviewer.model.task.RegenerateDiagramsJob;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.ProjectDomainMirror;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.ProjectDomainMirrorService;
import io.domainlifecycles.diagramviewer.service.RegenerateDiagramsJobService;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DiagramRegenerationTask {

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
        List<RegenerateDiagramsJob> allJobs = regenerateDiagramsJobService.getAll();

        allJobs.forEach(job -> {
            final Project project = job.getProject();

            project.getDiagrams().forEach(diagram -> {
                ProjectDomainMirror projectDomainMirror = projectDomainMirrorService.getByProjectId(project.getId());
                diagramService.regenerate(diagram, projectDomainMirror.getDomainMirror());
            });

            regenerateDiagramsJobService.delete(job);
        });
    }
}
