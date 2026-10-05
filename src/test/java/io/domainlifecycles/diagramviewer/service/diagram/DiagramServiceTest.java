package io.domainlifecycles.diagramviewer.service.diagram;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.ProjectDomainMirror;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.repository.DiagramTypeNoteRepository;
import io.domainlifecycles.diagramviewer.rest.kroki.KrokiClient;
import io.domainlifecycles.diagramviewer.service.DiagramRegenerationService;
import io.domainlifecycles.staticanalysis.DomainCalls;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.DiagramServiceImpl;
import io.domainlifecycles.diagramviewer.service.ProjectDomainMirrorService;
import io.domainlifecycles.diagramviewer.service.ProjectModel;
import io.domainlifecycles.diagramviewer.service.ProjectModelCache;
import io.domainlifecycles.diagramviewer.util.DiagrammerUtils;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.mirror.api.DomainMirror;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiagramServiceTest {

    @Mock
    ProjectModelCache projectModelCache;

    @Mock
    DiagramRepository repository;

    @Mock
    DiagramTypeNoteRepository diagramTypeNoteRepository;

    @Mock
    KrokiClient krokiClient;

    @Mock
    ProjectDomainMirrorService projectDomainMirrorService;

    @Mock
    Supplier<Optional<DomainCalls>> domainCallsLoader;

    DiagramService diagramService;

    DiagramRegenerationService diagramRegenerationService;

    @BeforeEach
    void setUp() {
        diagramService = new DiagramServiceImpl("/tmp/diagrams", projectModelCache, repository, diagramTypeNoteRepository, krokiClient, 1, 1000, 1024);
        diagramRegenerationService = new DiagramRegenerationService(projectModelCache, diagramService);
    }

    @Test
    public void Should_FindAllDiagramsWithProjectId() {

        // given
        UUID projectId = new UUID(0, 0);
        Diagram diagramMock = mock(Diagram.class);
        when(repository.findByProjectId(eq(projectId))).thenReturn(Set.of(diagramMock));

        // when
        Set<Diagram> diagramsResult = diagramService.findAll(projectId);

        // then
        verify(repository, times(1)).findByProjectId(eq(projectId));
        assertThat(diagramsResult).containsExactly(diagramMock);
    }

    @Test
    public void Should_UpdateDiagramModel() {

        // given
        Diagram diagram = mock(Diagram.class);
        when(repository.findByProjectIdAndName(any(), any())).thenReturn(List.of());
        when(repository.save(diagram)).thenReturn(diagram);

        // when
        Diagram result = diagramService.updateModel(diagram);

        // then
        verify(repository).save(diagram);
        assertThat(result).isEqualTo(diagram);
    }

    @Test
    public void Should_ThrowDiagramViewerExceptionOnUpdate_When_DiagramWithNameExistsAndDiagramNameHasChanged() {

        // given
        UUID diagramId = new UUID(0, 0);

        String newDiagramName = "newDiagramName.svg";

        Diagram newDiagramState = mock(Diagram.class);
        when(newDiagramState.getId()).thenReturn(diagramId);
        when(newDiagramState.getName()).thenReturn(newDiagramName);

        Diagram existingDiagramWithSameNameAsNew = mock(Diagram.class);

        when(repository.findByProjectIdAndName(any(), eq(newDiagramName))).thenReturn(List.of(existingDiagramWithSameNameAsNew));

        // when
        assertThatThrownBy(() -> diagramService.updateModelAndImage(newDiagramState))
            .isInstanceOf(DiagramViewerException.class)
            .hasMessage("Diagram with name '" + newDiagramName + "' already exists. Please choose a different name.");
    }

    @Test
    public void Should_ThrowDiagramViewerExceptionOnUpdate_When_DiagramIsNewButDiagramWithNameExists() {

        // given
        String newDiagramName = "newDiagramName.svg";

        Diagram newDiagramState = mock(Diagram.class);
        when(newDiagramState.getName()).thenReturn(newDiagramName);

        Diagram existingDiagramWithSameNameAsNew = mock(Diagram.class);

        when(repository.findByProjectIdAndName(any(), eq(newDiagramName))).thenReturn(List.of(existingDiagramWithSameNameAsNew));

        // when
        assertThatThrownBy(() -> diagramService.updateModel(newDiagramState))
            .isInstanceOf(DiagramViewerException.class)
            .hasMessage("Diagram with name '" + newDiagramName + "' already exists. Please choose a different name.");
    }

    @Test
    void Should_AcceptDiagramName_When_ItIsOnlyTakenInAnotherProject() {

        // given: a new diagram of one project, named like a diagram of another project
        UUID projectId = UUID.randomUUID();
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(projectId);
        Diagram newDiagram = mock(Diagram.class);
        when(newDiagram.getName()).thenReturn("Buchung - Aggregates");
        when(newDiagram.getProject()).thenReturn(project);
        when(repository.findByProjectIdAndName(projectId, "Buchung - Aggregates")).thenReturn(List.of());
        when(repository.save(newDiagram)).thenReturn(newDiagram);

        // when
        Diagram saved = diagramService.updateModel(newDiagram);

        // then: names are only checked within the diagram's own project
        assertThat(saved).isSameAs(newDiagram);
        verify(repository).findByProjectIdAndName(projectId, "Buchung - Aggregates");
    }

    @Test
    void Should_AcceptDiagramName_When_ItIsOnlyTakenInAnotherFolder() {

        // given: an "Aggregates" diagram in each bounded context folder
        UUID projectId = UUID.randomUUID();
        Project project = Project.builder().id(projectId).name("p").build();
        DiagramDirectory buchung = DiagramDirectory.builder().id(UUID.randomUUID()).name("Buchung").build();
        DiagramDirectory zimmer = DiagramDirectory.builder().id(UUID.randomUUID()).name("Zimmer").build();
        Diagram existing = Diagram.builder().id(UUID.randomUUID()).name("Aggregates").project(project).diagramDirectory(buchung).build();
        Diagram newDiagram = Diagram.builder().name("Aggregates").project(project).diagramDirectory(zimmer).build();
        when(repository.findByProjectIdAndName(projectId, "Aggregates")).thenReturn(List.of(existing));
        when(repository.save(newDiagram)).thenReturn(newDiagram);

        // when / then
        assertThat(diagramService.updateModel(newDiagram)).isSameAs(newDiagram);
    }

    @Test
    void Should_RejectDiagramName_When_ItIsTakenInTheSameFolder() {

        // given
        UUID projectId = UUID.randomUUID();
        Project project = Project.builder().id(projectId).name("p").build();
        DiagramDirectory buchung = DiagramDirectory.builder().id(UUID.randomUUID()).name("Buchung").build();
        Diagram existing = Diagram.builder().id(UUID.randomUUID()).name("Aggregates").project(project).diagramDirectory(buchung).build();
        Diagram newDiagram = Diagram.builder().name("Aggregates").project(project).diagramDirectory(buchung).build();
        when(repository.findByProjectIdAndName(projectId, "Aggregates")).thenReturn(List.of(existing));

        // when / then
        assertThatThrownBy(() -> diagramService.updateModel(newDiagram))
            .isInstanceOf(DiagramViewerException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    void Should_ThrowDiagramViewerException_When_DiagramUpdateAndNameAlreadyExistsAndChanged() {

        // given
        String diagramFileName = "diagramName";

        Diagram diagram = mock(Diagram.class);
        when(diagram.getId()).thenReturn(UUID.randomUUID());
        when(diagram.getName()).thenReturn(diagramFileName);

        Diagram existing = mock(Diagram.class);

        when(repository.findByProjectIdAndName(any(), eq(diagramFileName))).thenReturn(List.of(existing));

        // when
        // then
        assertThatThrownBy(() -> diagramService.updateModel(diagram))
            .isInstanceOf(DiagramViewerException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    void Should_UpdateModelAndImage() {

        // given
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(UUID.randomUUID());

        Diagram diagram = mock(Diagram.class);
        when(diagram.getName()).thenReturn("diagramName.svg");
        when(diagram.getDiagramStylingConfiguration()).thenReturn(mock(DiagramStylingConfiguration.class));
        when(diagram.getDomainModelVisibility()).thenReturn(mock(DomainModelVisibility.class));
        when(diagram.getProject()).thenReturn(project);

        when(repository.findByProjectIdAndName(any(), any())).thenReturn(List.of());
        when(repository.save(diagram)).thenReturn(diagram);
        ProjectModel model = projectModel(null);
        when(projectModelCache.get(any())).thenReturn(model);
        when(krokiClient.convert(any())).thenReturn("filedata".getBytes());

        try(MockedStatic<DiagrammerUtils> diagrammerUtilsMocked = Mockito.mockStatic(DiagrammerUtils.class);
            MockedStatic<FileIOUtils> fileIOUtilsMocked = Mockito.mockStatic(FileIOUtils.class)) {

            diagrammerUtilsMocked.when(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any(), any())).thenReturn("testNomnoml");

            // when
            Diagram result = diagramService.updateModelAndImage(diagram);

            // then
            verify(repository, times(1)).findByProjectIdAndName(any(), any());
            verify(repository, times(1)).save(eq(diagram));
            verify(projectModelCache, times(1)).get(any());
            verify(krokiClient, times(1)).convert(any());
            diagrammerUtilsMocked.verify(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any(), any()));
            fileIOUtilsMocked.verify(() -> FileIOUtils.saveFile(any(), any()));

            assertThat(result).isEqualTo(diagram);
        }
    }

    @Test
    void Should_NotRequestDomainCalls_When_RenderingDiagramWithoutFlowFilter() {

        // given
        Diagram diagram = diagramToRender(new DomainModelVisibility());

        try(MockedStatic<DiagrammerUtils> diagrammerUtilsMocked = Mockito.mockStatic(DiagrammerUtils.class);
            MockedStatic<FileIOUtils> fileIOUtilsMocked = Mockito.mockStatic(FileIOUtils.class)) {
            diagrammerUtilsMocked.when(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any(), any())).thenReturn("testNomnoml");

            // when
            diagramService.updateModelAndImage(diagram);

            // then: the (possibly large) static analysis result is neither loaded nor handed over
            verify(domainCallsLoader, never()).get();
            diagrammerUtilsMocked.verify(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any(), isNull()));
        }
    }

    @Test
    void Should_RequestDomainCalls_When_RenderingDiagramWithFlowFilter() {

        // given
        Diagram diagram = diagramToRender(new DomainModelVisibility()
            .replaceIncludeFlowsTo(Set.of("some.Type")));
        DomainCalls domainCalls = mock(DomainCalls.class);
        ProjectModel model = projectModel(domainCalls);
        when(projectModelCache.get(any())).thenReturn(model);

        try(MockedStatic<DiagrammerUtils> diagrammerUtilsMocked = Mockito.mockStatic(DiagrammerUtils.class);
            MockedStatic<FileIOUtils> fileIOUtilsMocked = Mockito.mockStatic(FileIOUtils.class)) {
            diagrammerUtilsMocked.when(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any(), any())).thenReturn("testNomnoml");

            // when
            diagramService.updateModelAndImage(diagram);

            // then
            verify(domainCallsLoader, times(1)).get();
            diagrammerUtilsMocked.verify(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any(), eq(domainCalls)));
        }
    }

    @Test
    void Should_GenerateTheNomnomlSourceOfTheDiagram_WithTheStaticAnalysisResult() {
        // given
        Diagram diagram = Diagram.builder().name("diagram").build();
        DomainMirror domainMirror = mock(DomainMirror.class);
        DomainCalls domainCalls = mock(DomainCalls.class);

        try(MockedStatic<DiagrammerUtils> diagrammerUtilsMocked = Mockito.mockStatic(DiagrammerUtils.class)) {
            diagrammerUtilsMocked.when(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any(), any())).thenReturn("testNomnoml");

            // when
            String nomnoml = diagramService.generateNomnoml(domainMirror, domainCalls, diagram);

            // then: the source as rendered, without rendering an image
            assertThat(nomnoml).isEqualTo("testNomnoml");
            diagrammerUtilsMocked.verify(() -> DiagrammerUtils.generateNomnoml(eq(domainMirror), any(), any(), any(), eq(domainCalls)));
            verifyNoInteractions(krokiClient);
        }
    }

    /**
     * A project model whose static analysis result is loaded via the {@link #domainCallsLoader} mock.
     */
    private ProjectModel projectModel(DomainCalls domainCalls) {
        lenient().when(domainCallsLoader.get()).thenReturn(Optional.ofNullable(domainCalls));
        return new ProjectModel(Instant.now(), mock(DomainMirror.class), List.of(), List.of(), true, domainCallsLoader);
    }

    private Diagram diagramToRender(DomainModelVisibility visibility) {
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(UUID.randomUUID());
        Diagram diagram = mock(Diagram.class);
        when(diagram.getName()).thenReturn("diagramName");
        when(diagram.getDiagramStylingConfiguration()).thenReturn(mock(DiagramStylingConfiguration.class));
        when(diagram.getDomainModelVisibility()).thenReturn(visibility);
        when(diagram.getProject()).thenReturn(project);
        when(repository.findByProjectIdAndName(any(), any())).thenReturn(List.of());
        when(repository.save(diagram)).thenReturn(diagram);
        ProjectModel model = projectModel(null);
        when(projectModelCache.get(any())).thenReturn(model);
        when(krokiClient.convert(any())).thenReturn("filedata".getBytes());
        return diagram;
    }

    @Test
    void Should_RenameDiagram() {

        // given
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(UUID.randomUUID());

        Diagram diagram = mock(Diagram.class);
        when(diagram.getProject()).thenReturn(project);
        when(diagram.getName()).thenReturn("diagramName.svg");

        when(repository.findByProjectIdAndName(any(), any())).thenReturn(List.of());
        when(repository.save(diagram)).thenReturn(diagram);

        try(MockedStatic<DiagrammerUtils> diagrammerUtilsMocked = Mockito.mockStatic(DiagrammerUtils.class);
            MockedStatic<FileIOUtils> fileIOUtilsMocked = Mockito.mockStatic(FileIOUtils.class)) {

            // when
            Diagram result = diagramService.rename(diagram, "newDiagramName");

            // then
            verify(repository, times(1)).findByProjectIdAndName(any(), any());
            verify(repository, times(1)).save(eq(diagram));

            // the image is named after the diagram's id and keeps its name
            fileIOUtilsMocked.verify(() -> FileIOUtils.renameFile(any(), any()), never());
            fileIOUtilsMocked.verify(() -> FileIOUtils.saveFile(any(), any()), never());
            diagrammerUtilsMocked.verify(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any(), any()), never());

            assertThat(result).isEqualTo(diagram);
        }
    }

    @Test
    void Should_CreateDiagram() {

        // given
        Project projectMock = mock(Project.class);
        when(projectMock.getId()).thenReturn(UUID.randomUUID());

        DomainModelVisibility domainModelVisibilityMock = mock(DomainModelVisibility.class);
        DiagramStylingConfiguration diagramStylingConfigurationMock = mock(DiagramStylingConfiguration.class);

        when(repository.findByProjectIdAndName(any(), any())).thenReturn(List.of());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ProjectModel model = projectModel(null);
        when(projectModelCache.get(any())).thenReturn(model);
        when(krokiClient.convert(any())).thenReturn("filedata".getBytes());

        try(MockedStatic<DiagrammerUtils> diagrammerUtilsMocked = Mockito.mockStatic(DiagrammerUtils.class);
            MockedStatic<FileIOUtils> fileIOUtilsMocked = Mockito.mockStatic(FileIOUtils.class)) {

            diagrammerUtilsMocked.when(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any(), any())).thenReturn(
                "testNomnoml");

            // when
            Diagram result = diagramService.create(
                projectMock,
                "diagramName",
                domainModelVisibilityMock,
                diagramStylingConfigurationMock
            );

            // then
            verify(repository).save(any());
            verify(krokiClient).convert(any());
            verify(projectMock).addDiagram(any());

            diagrammerUtilsMocked.verify(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any(), any()));
            fileIOUtilsMocked.verify(() -> FileIOUtils.saveFile(any(), any()));

            assertThat(result.getName()).isEqualTo("diagramName");
        }
    }

    @Test
    void Should_RegenerateDiagram() {

        // given
        Project projectMock = mock(Project.class);
        when(projectMock.getId()).thenReturn(UUID.randomUUID());

        Diagram diagramMock = mock(Diagram.class);
        when(diagramMock.getName()).thenReturn("diagramName.svg");
        when(diagramMock.getProject()).thenReturn(projectMock);
        when(diagramMock.getId()).thenReturn(UUID.randomUUID());

        when(krokiClient.convert(any())).thenReturn("img".getBytes());

        ProjectModel model = projectModel(null);

        when(projectModelCache.get(any())).thenReturn(model);

        try(MockedStatic<DiagrammerUtils> diagrammerUtilsMocked = Mockito.mockStatic(DiagrammerUtils.class);
            MockedStatic<FileIOUtils> fileIOUtilsMocked = Mockito.mockStatic(FileIOUtils.class)) {

            diagrammerUtilsMocked.when(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any(), any())).thenReturn(
                "testNomnoml");

            // when
            diagramRegenerationService.regenerate(diagramMock);

            // then
            verify(krokiClient).convert(any());

            diagrammerUtilsMocked.verify(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any(), any()));
            fileIOUtilsMocked.verify(() -> FileIOUtils.saveFile(any(), any()));
        }
    }

    @Test
    void Should_ThrowDiagramViewerException_When_GenerateNomnomlThrowsIllegalStateException() {

        // given
        String errorMessage = "Error in Nomnoml generation";
        IllegalStateException errorCause = new IllegalStateException(errorMessage);
        Project projectMock = mock(Project.class);
        when(projectMock.getId()).thenReturn(UUID.randomUUID());

        Diagram diagramMock = mock(Diagram.class);
        when(diagramMock.getId()).thenReturn(UUID.randomUUID());
        when(diagramMock.getProject()).thenReturn(projectMock);

        when(repository.save(diagramMock)).thenReturn(diagramMock);
        ProjectModel model = projectModel(null);
        when(projectModelCache.get(any())).thenReturn(model);

        try(MockedStatic<DiagrammerUtils> diagrammerUtilsMocked = Mockito.mockStatic(DiagrammerUtils.class)) {

            diagrammerUtilsMocked.when(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any(), any())).thenThrow(errorCause);

            // when
            assertThatThrownBy(() -> diagramService.updateModelAndImage(diagramMock)).isInstanceOf(DiagramViewerException.class).hasMessage(errorMessage).hasCause(errorCause);

            // then
            diagrammerUtilsMocked.verify(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any(), any()));
        }
    }

    @Test
    void Should_ThrowDiagramViewerException_When_SaveFileThrowsIOException() {

        // given
        Project projectMock = mock(Project.class);
        when(projectMock.getId()).thenReturn(UUID.randomUUID());

        Diagram diagramMock = mock(Diagram.class);
        when(diagramMock.getProject()).thenReturn(projectMock);
        when(diagramMock.getName()).thenReturn("diagramName.svg");
        when(diagramMock.getProject()).thenReturn(projectMock);

        when(repository.save(diagramMock)).thenReturn(diagramMock);
        ProjectModel model = projectModel(null);
        when(projectModelCache.get(any())).thenReturn(model);

        when(krokiClient.convert(any())).thenReturn("img".getBytes());

        try(MockedStatic<DiagrammerUtils> diagrammerUtilsMocked = Mockito.mockStatic(DiagrammerUtils.class);
            MockedStatic<FileIOUtils> fileIOUtilsMocked = Mockito.mockStatic(FileIOUtils.class)) {

            diagrammerUtilsMocked.when(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any(), any())).thenReturn(
                "testNomnoml");
            fileIOUtilsMocked.when(() -> FileIOUtils.saveFile(any(), any())).thenThrow(IOException.class);

            // when
            assertThatThrownBy(() -> diagramService.updateModelAndImage(diagramMock))
                .isInstanceOf(DiagramViewerException.class)
                .hasMessageContaining("Could not save diagram to ")
                .hasCauseInstanceOf(IOException.class);

            // then
            verify(krokiClient).convert(any());

            diagrammerUtilsMocked.verify(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any(), any()));
            fileIOUtilsMocked.verify(() -> FileIOUtils.saveFile(any(), any()));
        }
    }

    @Test
    void Should_DeleteFilesFromFilesystem_Successfully() {
        try(MockedStatic<FileIOUtils> fileIOUtilsMock = Mockito.mockStatic(FileIOUtils.class)) {

            // when
            diagramService.deleteFilesFromFilesystem("projectId");

            // then
            fileIOUtilsMock.verify(() -> FileIOUtils.deleteDirectoryRecursively(any()));
        }
    }

    @Test
    void Should_ThrowDiagramViewerException_When_DeleteDirectoryRecursivelyThrowsIOException() throws IOException {
        try(MockedStatic<FileIOUtils> fileIOUtilsMock = Mockito.mockStatic(FileIOUtils.class)) {

            // given
            fileIOUtilsMock.when(() -> FileIOUtils.deleteDirectoryRecursively(any())).thenThrow(IOException.class);

            // when
            assertThatThrownBy(() -> diagramService.deleteFilesFromFilesystem("pid"))
                .isInstanceOf(DiagramViewerException.class);

            // then
            fileIOUtilsMock.verify(() -> FileIOUtils.deleteDirectoryRecursively(any()));
        }
    }
}