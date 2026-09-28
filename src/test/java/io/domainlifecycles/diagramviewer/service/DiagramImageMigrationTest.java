package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DiagramImageMigrationTest {

    @TempDir
    Path diagramsLocation;

    @Test
    void Should_RenameImagesStoredUnderTheDiagramName_ToTheDiagramId() throws Exception {

        // given: an image from before, named after its diagram, and one already named after its id
        Project project = Project.builder().id(UUID.randomUUID()).name("p").build();
        Diagram legacy = Diagram.builder().id(UUID.randomUUID()).name("Aggregates").project(project).build();
        Diagram current = Diagram.builder().id(UUID.randomUUID()).name("Commands").project(project).build();
        Path projectDirectory = Files.createDirectories(diagramsLocation.resolve(project.getId().toString()));
        Files.writeString(projectDirectory.resolve("Aggregates.svg"), "<svg>legacy</svg>");
        Files.writeString(projectDirectory.resolve(current.getId() + ".svg"), "<svg>current</svg>");
        DiagramRepository repository = mock(DiagramRepository.class);
        when(repository.findAll()).thenReturn(List.of(legacy, current));

        // when
        new DiagramImageMigration(repository, diagramsLocation.toString()).run(null);

        // then
        assertThat(projectDirectory.resolve(legacy.getId() + ".svg")).hasContent("<svg>legacy</svg>");
        assertThat(projectDirectory.resolve("Aggregates.svg")).doesNotExist();
        assertThat(projectDirectory.resolve(current.getId() + ".svg")).hasContent("<svg>current</svg>");
    }
}
