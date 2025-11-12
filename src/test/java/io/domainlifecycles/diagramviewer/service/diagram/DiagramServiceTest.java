package io.domainlifecycles.diagramviewer.service.diagram;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.ProjectDomainMirror;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.repository.DiagramTypeNoteRepository;
import io.domainlifecycles.diagramviewer.rest.kroki.FileType;
import io.domainlifecycles.diagramviewer.rest.kroki.KrokiClient;
import io.domainlifecycles.diagramviewer.service.DiagramRegenerationService;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.DiagramServiceImpl;
import io.domainlifecycles.diagramviewer.service.ProjectDomainMirrorService;
import io.domainlifecycles.diagramviewer.util.DiagrammerUtils;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.mirror.api.DomainMirror;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiagramServiceTest {

    @Mock
    SessionStorage sessionStorage;

    @Mock
    DiagramRepository repository;

    @Mock
    DiagramTypeNoteRepository diagramTypeNoteRepository;

    @Mock
    KrokiClient krokiClient;

    @Mock
    ProjectDomainMirrorService projectDomainMirrorService;

    DiagramService diagramService;

    DiagramRegenerationService diagramRegenerationService;

    @BeforeEach
    void setUp() {
        diagramService = new DiagramServiceImpl("/tmp/diagrams", sessionStorage, repository, diagramTypeNoteRepository, krokiClient);
        diagramRegenerationService = new DiagramRegenerationService(projectDomainMirrorService, diagramService);
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
        when(repository.findByFileName(any())).thenReturn(Optional.empty());
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

        String oldDiagramName = "oldDiagramName.svg";
        String newDiagramName = "newDiagramName.svg";

        Diagram newDiagramState = mock(Diagram.class);
        when(newDiagramState.getId()).thenReturn(diagramId);
        when(newDiagramState.getFileName()).thenReturn(newDiagramName);

        Diagram oldDiagramState = mock(Diagram.class);
        when(oldDiagramState.getFileName()).thenReturn(oldDiagramName);

        Diagram existingDiagramWithSameNameAsNew = mock(Diagram.class);
        when(existingDiagramWithSameNameAsNew.getFileName()).thenReturn(newDiagramName);

        when(repository.findByFileName(eq(newDiagramName))).thenReturn(Optional.of(existingDiagramWithSameNameAsNew));
        when(repository.findById(eq(diagramId))).thenReturn(Optional.of(oldDiagramState));

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
        when(newDiagramState.getFileName()).thenReturn(newDiagramName);

        Diagram existingDiagramWithSameNameAsNew = mock(Diagram.class);
        when(existingDiagramWithSameNameAsNew.getFileName()).thenReturn(newDiagramName);

        when(repository.findByFileName(eq(newDiagramName))).thenReturn(Optional.of(existingDiagramWithSameNameAsNew));

        // when
        assertThatThrownBy(() -> diagramService.updateModel(newDiagramState))
            .isInstanceOf(DiagramViewerException.class)
            .hasMessage("Diagram with name '" + newDiagramName + "' already exists. Please choose a different name.");
    }

    @Test
    void Should_ThrowDiagramViewerException_When_DiagramUpdateAndNameAlreadyExistsAndChanged() {

        // given
        String diagramFileName = "diagramName";

        Diagram diagram = mock(Diagram.class);
        when(diagram.getId()).thenReturn(UUID.randomUUID());
        when(diagram.getFileName()).thenReturn(diagramFileName);

        Diagram existing = mock(Diagram.class);
        when(existing.getFileName()).thenReturn(diagramFileName);

        when(repository.findByFileName(diagramFileName)).thenReturn(Optional.of(existing));
        when(repository.findById(any())).thenReturn(Optional.of(mock(Diagram.class)));

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
        when(diagram.getFileName()).thenReturn("diagramName.svg");
        when(diagram.getDiagramStylingConfiguration()).thenReturn(mock(DiagramStylingConfiguration.class));
        when(diagram.getDomainModelVisibility()).thenReturn(mock(DomainModelVisibility.class));
        when(diagram.getProject()).thenReturn(project);

        when(repository.findByFileName(any())).thenReturn(Optional.empty());
        when(repository.save(diagram)).thenReturn(diagram);
        when(sessionStorage.getDomainMirror(any())).thenReturn(mock(DomainMirror.class));
        when(krokiClient.convertTo(any(), any())).thenReturn("filedata".getBytes());

        try(MockedStatic<DiagrammerUtils> diagrammerUtilsMocked = Mockito.mockStatic(DiagrammerUtils.class);
            MockedStatic<FileIOUtils> fileIOUtilsMocked = Mockito.mockStatic(FileIOUtils.class)) {

            diagrammerUtilsMocked.when(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any())).thenReturn("testNomnoml");

            // when
            Diagram result = diagramService.updateModelAndImage(diagram);

            // then
            verify(repository, times(1)).findByFileName(any());
            verify(repository, times(1)).save(eq(diagram));
            verify(sessionStorage, times(1)).getDomainMirror(any());
            verify(krokiClient, times(1)).convertTo(any(), any());
            diagrammerUtilsMocked.verify(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any()));
            fileIOUtilsMocked.verify(() -> FileIOUtils.saveFile(any(), any()));

            assertThat(result).isEqualTo(diagram);
        }
    }

    @Test
    void Should_RenameDiagram() {

        // given
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(UUID.randomUUID());

        Diagram diagram = mock(Diagram.class);
        when(diagram.getProject()).thenReturn(project);
        when(diagram.getFileName()).thenReturn("diagramName.svg");
        when(diagram.getFileType()).thenReturn(FileType.SVG);
        when(diagram.getDiagramStylingConfiguration()).thenReturn(mock(DiagramStylingConfiguration.class));
        when(diagram.getDomainModelVisibility()).thenReturn(mock(DomainModelVisibility.class));

        when(repository.findByFileName(any())).thenReturn(Optional.empty());
        when(repository.save(diagram)).thenReturn(diagram);
        when(sessionStorage.getDomainMirror(any())).thenReturn(mock(DomainMirror.class));
        when(krokiClient.convertTo(any(), any())).thenReturn("filedata".getBytes());

        try(MockedStatic<DiagrammerUtils> diagrammerUtilsMocked = Mockito.mockStatic(DiagrammerUtils.class);
            MockedStatic<FileIOUtils> fileIOUtilsMocked = Mockito.mockStatic(FileIOUtils.class)) {

            diagrammerUtilsMocked.when(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any())).thenReturn("testNomnoml");

            // when
            Diagram result = diagramService.rename(diagram, "newDiagramName");

            // then
            verify(repository, times(1)).findByFileName(any());
            verify(repository, times(1)).save(eq(diagram));
            verify(sessionStorage, times(1)).getDomainMirror(any());
            verify(krokiClient, times(1)).convertTo(any(), any());

            fileIOUtilsMocked.verify(() -> FileIOUtils.renameFile(any(), any()));
            diagrammerUtilsMocked.verify(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any()));
            fileIOUtilsMocked.verify(() -> FileIOUtils.saveFile(any(), any()));

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

        when(repository.findByFileName(any())).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sessionStorage.getDomainMirror(any())).thenReturn(mock(DomainMirror.class));
        when(krokiClient.convertTo(any(), any())).thenReturn("filedata".getBytes());

        try(MockedStatic<DiagrammerUtils> diagrammerUtilsMocked = Mockito.mockStatic(DiagrammerUtils.class);
            MockedStatic<FileIOUtils> fileIOUtilsMocked = Mockito.mockStatic(FileIOUtils.class)) {

            diagrammerUtilsMocked.when(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any())).thenReturn(
                "testNomnoml");

            // when
            Diagram result = diagramService.create(
                projectMock,
                "diagramName",
                FileType.SVG,
                domainModelVisibilityMock,
                diagramStylingConfigurationMock
            );

            // then
            verify(repository).save(any());
            verify(krokiClient).convertTo(any(), any());
            verify(projectMock).addDiagram(any());

            diagrammerUtilsMocked.verify(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any()));
            fileIOUtilsMocked.verify(() -> FileIOUtils.saveFile(any(), any()));

            assertThat(result.getFileName()).isEqualTo("diagramName.svg");
        }
    }

    @Test
    void Should_RegenerateDiagram() {

        // given
        Project projectMock = mock(Project.class);
        when(projectMock.getId()).thenReturn(UUID.randomUUID());

        Diagram diagramMock = mock(Diagram.class);
        when(diagramMock.getFileName()).thenReturn("diagramName.svg");
        when(diagramMock.getProject()).thenReturn(projectMock);
        when(diagramMock.getId()).thenReturn(UUID.randomUUID());

        when(krokiClient.convertTo(any(), any())).thenReturn("img".getBytes());

        ProjectDomainMirror projectDomainMirrorMock = mock(ProjectDomainMirror.class);
        DomainMirror domainMirrorMock = mock(DomainMirror.class);
        when(projectDomainMirrorMock.getDomainMirror()).thenReturn(domainMirrorMock);
        when(projectDomainMirrorService.getByProjectId(any())).thenReturn(projectDomainMirrorMock);

        try(MockedStatic<DiagrammerUtils> diagrammerUtilsMocked = Mockito.mockStatic(DiagrammerUtils.class);
            MockedStatic<FileIOUtils> fileIOUtilsMocked = Mockito.mockStatic(FileIOUtils.class)) {

            diagrammerUtilsMocked.when(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any())).thenReturn(
                "testNomnoml");

            // when
            diagramRegenerationService.regenerate(diagramMock);

            // then
            verify(krokiClient).convertTo(any(), any());

            diagrammerUtilsMocked.verify(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any()));
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

        try(MockedStatic<DiagrammerUtils> diagrammerUtilsMocked = Mockito.mockStatic(DiagrammerUtils.class)) {

            diagrammerUtilsMocked.when(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any())).thenThrow(errorCause);

            // when
            assertThatThrownBy(() -> diagramService.updateModelAndImage(diagramMock)).isInstanceOf(DiagramViewerException.class).hasMessage(errorMessage).hasCause(errorCause);

            // then
            diagrammerUtilsMocked.verify(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any()));
        }
    }

    @Test
    void Should_ThrowDiagramViewerException_When_SaveFileThrowsIOException() {

        // given
        Project projectMock = mock(Project.class);
        when(projectMock.getId()).thenReturn(UUID.randomUUID());

        Diagram diagramMock = mock(Diagram.class);
        when(diagramMock.getProject()).thenReturn(projectMock);
        when(diagramMock.getFileName()).thenReturn("diagramName.svg");
        when(diagramMock.getProject()).thenReturn(projectMock);

        when(repository.save(diagramMock)).thenReturn(diagramMock);

        when(krokiClient.convertTo(any(), any())).thenReturn("img".getBytes());

        try(MockedStatic<DiagrammerUtils> diagrammerUtilsMocked = Mockito.mockStatic(DiagrammerUtils.class);
            MockedStatic<FileIOUtils> fileIOUtilsMocked = Mockito.mockStatic(FileIOUtils.class)) {

            diagrammerUtilsMocked.when(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any())).thenReturn(
                "testNomnoml");
            fileIOUtilsMocked.when(() -> FileIOUtils.saveFile(any(), any())).thenThrow(IOException.class);

            // when
            assertThatThrownBy(() -> diagramService.updateModelAndImage(diagramMock))
                .isInstanceOf(DiagramViewerException.class)
                .hasMessageContaining("Could not save diagram to ")
                .hasCauseInstanceOf(IOException.class);

            // then
            verify(krokiClient).convertTo(any(), any());

            diagrammerUtilsMocked.verify(() -> DiagrammerUtils.generateNomnoml(any(), any(), any(), any()));
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