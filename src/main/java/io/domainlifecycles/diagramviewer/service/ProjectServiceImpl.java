package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.kroki.KrokiClient;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final String defaultTargetsDirectory;
    private final KrokiClient krokiClient;
    private final ProjectRepository repository;

    public ProjectServiceImpl(
        @Value("${targets.location}") String defaultTargetsDirectory,
        ProjectRepository repository,
        KrokiClient krokiClient) {

        this.defaultTargetsDirectory = defaultTargetsDirectory;
        this.krokiClient = krokiClient;
        this.repository = repository;
        initTargetsInDefaultDirectory();
    }

    @Override
    public Stream<Project> getAll() {
        Iterable<Project> allProjects = repository.findAll();
        return StreamSupport.stream(allProjects.spliterator(), false);
    }

    @Override
    public Project get(final String targetName) {
        return repository.findByProjectNameFull(targetName)
            .orElseThrow(() -> DiagramViewerException.fail(String.format("No project found with name: %s", targetName)));
    }

    private void initTargetsInDefaultDirectory(){
        if (defaultTargetsDirectory != null) {
            File dir = new File(defaultTargetsDirectory);

            if(dir.exists()) {
                Set<File> files = FileIOUtils.getAllFilesIncludingSubsequent(dir);
                // TODO: Add newly added .jar files to the Project DB so that they're picked up by the UI!
            }
        }
    }
}
