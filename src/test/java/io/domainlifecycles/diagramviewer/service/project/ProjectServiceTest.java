package io.domainlifecycles.diagramviewer.service.project;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.InvitedUser;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.RegisteredUser;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.DiagramServiceImpl;
import io.domainlifecycles.diagramviewer.service.DiagramTypeNoteService;
import io.domainlifecycles.diagramviewer.service.InvitedUserService;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.service.ProjectServiceImpl;
import io.domainlifecycles.diagramviewer.service.RegenerateDiagramsJobService;
import io.domainlifecycles.diagramviewer.service.RegisteredUserService;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.UploadFileType;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.mirror.api.DomainMirror;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    DiagramService diagramService;

    @Mock
    DiagramTypeNoteService diagramTypeNoteService;

    @Mock
    RegenerateDiagramsJobService regenerateDiagramsJobService;

    @Mock
    RegisteredUserService registeredUserService;

    @Mock
    InvitedUserService invitedUserService;

    @Mock
    SessionStorage sessionStorage;

    @Mock
    ProjectRepository repository;

    ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectServiceImpl("/tmp/diagrams", diagramService, diagramTypeNoteService,
            regenerateDiagramsJobService, registeredUserService, invitedUserService, sessionStorage, repository);
    }

    @Test
    void Should_GetAllProjectsAssignedToUser() {

        // given
        RegisteredUser registeredUserMock = mock(RegisteredUser.class);
        when(registeredUserMock.getId()).thenReturn(new UUID(0, 0));

        RegisteredUser anotherRegisteredUserMock = mock(RegisteredUser.class);
        when(anotherRegisteredUserMock.getId()).thenReturn(new UUID(0, 0));

        Project projectAssignedToRegisteredUserMock = mock(Project.class);
        when(projectAssignedToRegisteredUserMock.getAssignedRegisteredUsers()).thenReturn(Set.of(registeredUserMock));

        Project projectAssignedToAnotherRegisteredUserMock = mock(Project.class);
        when(projectAssignedToAnotherRegisteredUserMock.getAssignedRegisteredUsers()).thenReturn(
            Set.of(anotherRegisteredUserMock));

        when(repository.findAll()).thenReturn(
            List.of(projectAssignedToRegisteredUserMock, projectAssignedToAnotherRegisteredUserMock));

        // when
        Stream<Project> result = projectService.getAll(registeredUserMock);

        // then
        assertThat(result.toList().get(0)).isEqualTo(projectAssignedToRegisteredUserMock);
        verify(repository, times(1)).findAll();
    }

    @Test
    void Should_GetProjectByName_When_ProjectExists() {

        // given
        String projectName = "projectName";

        Project projectMock = mock(Project.class);
        when(projectMock.getName()).thenReturn(projectName);

        when(repository.findByName(eq(projectName))).thenReturn(Optional.of(projectMock));

        // when
        Project result = projectService.getByName(projectName);

        // then
        assertThat(result).isEqualTo(projectMock);
        verify(repository, times(1)).findByName(eq(projectName));
    }

    @Test
    void Should_ThrowDiagramViewerExceptionOnGetProjectByName_When_ProjectNotExists() {

        // given
        String projectName = "projectName";

        when(repository.findByName(eq(projectName))).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> projectService.getByName(projectName))
            .isInstanceOf(DiagramViewerException.class)
            .hasMessage(String.format("No project found with name: '%s'", projectName));

        // then
        verify(repository, times(1)).findByName(eq(projectName));
    }

    @Test
    void Should_UpdateDomainMirror_When_UserIsProjectCreator() {

        // given
        Set<String> domainModelPackages = Set.of("com.esentri");
        Path pathToFile = mock(Path.class);
        UploadFileType uploadFileType = UploadFileType.JAR;

        RegisteredUser registeredUserMock = mock(RegisteredUser.class);
        when(registeredUserMock.getId()).thenReturn(new UUID(0, 0));

        Project projectMock = mock(Project.class);
        when(projectMock.getCreator()).thenReturn(registeredUserMock);

        doNothing().when(sessionStorage).createOrUpdate(eq(projectMock), eq(domainModelPackages), eq(pathToFile),
            eq(uploadFileType));

        // when
        Project result = projectService.updateDomainMirror(projectMock, domainModelPackages, registeredUserMock,
            pathToFile, uploadFileType);

        // then
        assertThat(result).isEqualTo(projectMock);
        verify(sessionStorage, times(1)).createOrUpdate(eq(projectMock), eq(domainModelPackages), eq(pathToFile),
            eq(uploadFileType));
    }

    @Test
    void Should_ThrowDiagramViewerExceptionOnUpdateDomainMirror_When_UserIsNotProjectCreator() {

        // given
        Set<String> domainModelPackages = Set.of("com.esentri");
        Path pathToFile = mock(Path.class);
        UploadFileType uploadFileType = UploadFileType.JAR;

        String userMailAddress = "max.mustermann@gmail.com";
        RegisteredUser registeredUserMock = mock(RegisteredUser.class);
        when(registeredUserMock.getId()).thenReturn(new UUID(0, 0));
        when(registeredUserMock.getEmailAddress()).thenReturn(userMailAddress);

        RegisteredUser anotherRegisteredUserMock = mock(RegisteredUser.class);

        String projectName = "projectName";
        Project projectMock = mock(Project.class);
        when(projectMock.getName()).thenReturn(projectName);
        when(projectMock.getCreator()).thenReturn(anotherRegisteredUserMock);

        // when
        assertThatThrownBy(() -> projectService.updateDomainMirror(projectMock, domainModelPackages, registeredUserMock,
            pathToFile, uploadFileType))
            .isInstanceOf(DiagramViewerException.class)
            .hasMessage(
                "User '" + userMailAddress + "' is not allowed to update the domain mirror of project '" + projectName + "'. Only project admins are.");
    }


    @Test
    void Should_CreateProject_When_ProjectWithNameDoesNotAlreadyExist() {

        // given
        String projectName = "projectName";
        Set<String> domainModelPackages = Set.of("com.esentri");
        Path pathToFile = mock(Path.class);
        UploadFileType uploadFileType = UploadFileType.JAR;
        RegisteredUser registeredUserMock = mock(RegisteredUser.class);

        when(repository.findByName(eq(projectName))).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(mock(Project.class));
        doNothing().when(sessionStorage).createOrUpdate(any(Project.class), eq(domainModelPackages), eq(pathToFile),
            eq(uploadFileType));

        // when
        projectService.create(projectName, domainModelPackages, registeredUserMock, pathToFile, uploadFileType);

        // then
        verify(repository, times(1)).findByName(eq(projectName));
        verify(sessionStorage, times(1)).createOrUpdate(any(Project.class), eq(domainModelPackages), eq(pathToFile),
            eq(uploadFileType));
    }

    @Test
    void Should_ThrowDiagramViewerExceptionOnCreate_When_ProjectWithNameAlreadyExists() {

        // given
        String projectName = "projectName";
        Set<String> domainModelPackages = Set.of("com.esentri");
        Path pathToFile = mock(Path.class);
        UploadFileType uploadFileType = UploadFileType.JAR;
        RegisteredUser registeredUserMock = mock(RegisteredUser.class);

        when(repository.findByName(eq(projectName))).thenReturn(Optional.of(mock(Project.class)));

        // when
        assertThatThrownBy(() -> projectService.create(projectName, domainModelPackages, registeredUserMock, pathToFile,
            uploadFileType))
            .isInstanceOf(DiagramViewerException.class)
            .hasMessage(String.format("Project with name '%s' already exists. Please choose a different filename.",
                projectName));

        // then
        verify(repository, times(1)).findByName(eq(projectName));
    }

    @Test
    void Should_UpdateDomainModel_When_ProjectWithNameAlreadyExists() {

        // given
        RegisteredUser registeredUserMock = mock(RegisteredUser.class);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(registeredUserMock, null));

        String projectName = "projectName";
        Project projectMock = mock(Project.class);
        when(projectMock.getName()).thenReturn(projectName);
        when(projectMock.getAssignedRegisteredUsers()).thenReturn(Set.of(registeredUserMock));

        DomainMirror domainMirrorMock = mock(DomainMirror.class);

        when(repository.findByName(eq(projectName))).thenReturn(Optional.of(projectMock));

        // when
        projectService.createOrUpdateDomainModel(projectName, domainMirrorMock);

        // then
        verify(repository, times(1)).findByName(eq(projectName));
        verify(projectMock, times(1)).setChangedAt(any());
        verify(repository, times(1)).save(projectMock);
        verify(sessionStorage, times(1)).createOrUpdate(projectMock, domainMirrorMock);
    }

    @Test
    void Should_ThrowDiagramViewerExceptionOnCreateOrUpdateDomainModel_When_UserIsNotAssignedToProject() {

        // given
        RegisteredUser registeredUserMock = mock(RegisteredUser.class);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(registeredUserMock, null));

        String projectName = "projectName";
        Project projectMock = mock(Project.class);
        when(projectMock.getName()).thenReturn(projectName);

        DomainMirror domainMirrorMock = mock(DomainMirror.class);

        when(repository.findByName(eq(projectName))).thenReturn(Optional.of(projectMock));

        // when
        assertThatThrownBy(() -> projectService.createOrUpdateDomainModel(projectName, domainMirrorMock))
            .isInstanceOf(DiagramViewerException.class)
            .hasMessage("User has no access to project '" + projectName + "'");

        // then
        verify(repository, times(1)).findByName(eq(projectName));
    }

    @Test
    void Should_CreateDomainModel_When_ProjectWithNameDoesNotAlreadyExist() {

        // given
        RegisteredUser registeredUserMock = mock(RegisteredUser.class);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(registeredUserMock, null));

        String projectName = "projectName";
        DomainMirror domainMirrorMock = mock(DomainMirror.class);

        when(repository.findByName(eq(projectName))).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(mock(Project.class));

        // when
        projectService.createOrUpdateDomainModel(projectName, domainMirrorMock);

        // then
        verify(repository, times(1)).findByName(eq(projectName));
        verify(repository, times(1)).save(any(Project.class));
        verify(sessionStorage, times(1)).createOrUpdate(any(Project.class), eq(domainMirrorMock));
    }

    @Test
    void Should_RenameProject_When_UserIsProjectCreatorAndProjectWithNameDoesNotAlreadyExist() {

        // given
        RegisteredUser registeredUserMock = mock(RegisteredUser.class);

        String projectName = "projectName";
        Project projectMock = mock(Project.class);
        when(projectMock.getName()).thenReturn(projectName);
        when(projectMock.getCreator()).thenReturn(registeredUserMock);

        when(repository.findByName(eq(projectName))).thenReturn(Optional.empty());

        // when
        projectService.rename(projectMock, registeredUserMock, "newProjectName");

        // then
        verify(repository, times(1)).findByName(eq(projectName));
        verify(repository, times(1)).save(any(Project.class));
    }

    @Test
    void Should_AssignUserToProject_When_UserIsNotAlreadyAssignedAndUserKnown() {

        // given
        String emailAddress = "max.mustermann@gmail.com";

        RegisteredUser registeredUserMock = mock(RegisteredUser.class);
        when(registeredUserMock.getId()).thenReturn(new UUID(0, 0));

        RegisteredUser anotherRegisteredUserMock = mock(RegisteredUser.class);
        when(anotherRegisteredUserMock.getId()).thenReturn(new UUID(1, 1));
        when(anotherRegisteredUserMock.getEmailAddress()).thenReturn("another.mail@gmail.com");

        Project projectMock = mock(Project.class);
        when(projectMock.getAssignedRegisteredUsers()).thenReturn(Set.of(anotherRegisteredUserMock));

        when(registeredUserService.userKnown(eq(emailAddress))).thenReturn(true);
        when(registeredUserService.get(eq(emailAddress))).thenReturn(registeredUserMock);
        when(repository.save(eq(projectMock))).thenReturn(projectMock);

        // when
        projectService.assignUser(projectMock, emailAddress);

        // then
        verify(registeredUserService, times(1)).userKnown(eq(emailAddress));
        verify(registeredUserService, times(1)).get(eq(emailAddress));
        verify(projectMock, times(1)).assignUser(any());
        verify(repository, times(1)).save(projectMock);
    }

    @Test
    void Should_NotAssignUserToProject_When_UserIsAlreadyAssigned() {

        // given
        String emailAddress = "max.mustermann@gmail.com";

        RegisteredUser registeredUserMock = mock(RegisteredUser.class);
        when(registeredUserMock.getEmailAddress()).thenReturn(emailAddress);

        Project projectMock = mock(Project.class);
        when(projectMock.getAssignedRegisteredUsers()).thenReturn(Set.of(registeredUserMock));

        // when
        projectService.assignUser(projectMock, emailAddress);

        // then
        verifyNoInteractions(registeredUserService);
    }

    @Test
    void Should_AssignUserToProject_When_UserIsNotAlreadyAssignedAndUserNotKnown() {

        // given
        String emailAddress = "max.mustermann@gmail.com";

        InvitedUser invitedUserMock = mock(InvitedUser.class);

        RegisteredUser anotherRegisteredUserMock = mock(RegisteredUser.class);
        when(anotherRegisteredUserMock.getEmailAddress()).thenReturn("another.mail@gmail.com");

        Project projectMock = mock(Project.class);
        when(projectMock.getAssignedRegisteredUsers()).thenReturn(Set.of(anotherRegisteredUserMock));

        when(registeredUserService.userKnown(eq(emailAddress))).thenReturn(false);
        when(invitedUserService.getOrCreate(eq(emailAddress))).thenReturn(invitedUserMock);
        when(repository.save(eq(projectMock))).thenReturn(projectMock);

        // when
        projectService.assignUser(projectMock, emailAddress);

        // then
        verify(registeredUserService, times(1)).userKnown(eq(emailAddress));
        verify(invitedUserService, times(1)).getOrCreate(eq(emailAddress));
        verify(projectMock, times(1)).assignUser(any());
        verify(repository, times(1)).save(projectMock);
    }

    @Test
    void Should_ThrowDiagramViewerExceptionOnAssignUser_When_UserIsAlreadyAssigned() {

        // given
        RegisteredUser registeredUserMock = mock(RegisteredUser.class);
        String emailAddress = "max.mustermann@gmail.com";
        when(registeredUserMock.getEmailAddress()).thenReturn(emailAddress);

        Project projectMock = mock(Project.class);
        when(projectMock.getAssignedRegisteredUsers()).thenReturn(Set.of(registeredUserMock));

        // when
        assertThatThrownBy(() -> projectService.assignUser(projectMock, registeredUserMock))
            .hasMessage("User '" + emailAddress + "' is already assigned to project.");

        // then
        verifyNoInteractions(repository);
    }

    @Test
    void Should_NotUnassignUser_When_UserIsCreator() {

        // given
        RegisteredUser registeredUserMock = mock(RegisteredUser.class);
        String emailAddress = "max.mustermann@gmail.com";
        when(registeredUserMock.getEmailAddress()).thenReturn(emailAddress);

        Project projectMock = mock(Project.class);
        when(projectMock.getCreator()).thenReturn(registeredUserMock);

        // when
        projectService.unassignUser(projectMock, registeredUserMock);

        // then
        verifyNoInteractions(repository);
    }

    @Test
    void Should_UnassignUser_When_UserIsNotCreator() {

        // given
        RegisteredUser registeredUserMock = mock(RegisteredUser.class);
        String emailAddress = "max.mustermann@gmail.com";
        when(registeredUserMock.getEmailAddress()).thenReturn(emailAddress);

        RegisteredUser anotherRegisteredUserMock = mock(RegisteredUser.class);
        when(anotherRegisteredUserMock.getEmailAddress()).thenReturn("another.mail@gmail.com");

        Project projectMock = mock(Project.class);
        when(projectMock.getCreator()).thenReturn(anotherRegisteredUserMock);
        when(projectMock.getAssignedRegisteredUsers()).thenReturn(Set.of(registeredUserMock, anotherRegisteredUserMock));

        doNothing().when(projectMock).unassignUser(eq(registeredUserMock));
        when(repository.save(eq(projectMock))).thenReturn(projectMock);

        // when
        projectService.unassignUser(projectMock, registeredUserMock);

        // then
        verify(projectMock, times(1)).unassignUser(eq(registeredUserMock));
        verify(repository, times(1)).save(eq(projectMock));
        verifyNoInteractions(invitedUserService);
    }

    @Test
    void Should_UnassignAndDeleteUser_When_UserIsInvitedUserAndNotAssignedToAnyOtherProject() {

        // given
        InvitedUser invitedUserMock = mock(InvitedUser.class);

        Project projectMock = mock(Project.class);

        when(repository.save(eq(projectMock))).thenReturn(projectMock);
        when(invitedUserService.checkForRemoval(eq(invitedUserMock))).thenReturn(true);
        doNothing().when(invitedUserService).delete(eq(invitedUserMock));

        // when
        projectService.unassignUser(projectMock, invitedUserMock);

        // then
        verify(projectMock, times(1)).unassignUser(eq(invitedUserMock));
        verify(repository, times(1)).save(eq(projectMock));
        verify(invitedUserService, times(1)).checkForRemoval(eq(invitedUserMock));
        verify(invitedUserService, times(1)).delete(eq(invitedUserMock));
    }

    @Test
    void Should_DeleteProjectAndAllDependentObjects() {

        UUID projectId = new UUID(0, 0);
        String projectName = "projectName";

        Diagram firstDiagramMock = mock(Diagram.class);
        Diagram secondDiagramMock = mock(Diagram.class);

        Project projectMock = mock(Project.class);
        when(projectMock.getId()).thenReturn(projectId);
        when(projectMock.getDiagrams()).thenReturn(Set.of(firstDiagramMock, secondDiagramMock));
        when(projectMock.getName()).thenReturn(projectName);

        when(repository.findByName(eq(projectName))).thenReturn(Optional.of(projectMock));
        doNothing().when(projectMock).unassignAllUsers();
        doNothing().when(diagramTypeNoteService).delete(eq(firstDiagramMock));
        doNothing().when(diagramTypeNoteService).delete(eq(secondDiagramMock));
        doNothing().when(repository).delete(eq(projectMock));
        doNothing().when(sessionStorage).delete(eq(projectId));
        doNothing().when(diagramService).deleteFilesFromFilesystem(eq(projectId.toString()));

        // when
        projectService.delete(projectMock);

        // then
        verify(repository, times(1)).findByName(eq(projectName));
        verify(projectMock, times(1)).unassignAllUsers();
        verify(diagramTypeNoteService, times(1)).delete(eq(firstDiagramMock));
        verify(diagramTypeNoteService, times(1)).delete(eq(secondDiagramMock));
        verify(repository, times(1)).delete(eq(projectMock));
        verify(sessionStorage, times(1)).delete(eq(projectId));
        verify(diagramService, times(1)).deleteFilesFromFilesystem(eq(projectId.toString()));
    }

    @Test
    void Should_DeleteDiagram() {

        Project projectMock = mock(Project.class);
        when(projectMock.getId()).thenReturn(new UUID(0, 0));
        Diagram diagramMock = mock(Diagram.class);
        when(diagramMock.getFileName()).thenReturn("diagramName.svg");

        doNothing().when(diagramTypeNoteService).delete(eq(diagramMock));
        doNothing().when(regenerateDiagramsJobService).delete(eq(diagramMock));
        doNothing().when(projectMock).removeDiagram(eq(diagramMock));
        when(repository.save(eq(projectMock))).thenReturn(projectMock);

        try(MockedStatic<FileIOUtils> fileIOUtilsMocked = mockStatic(FileIOUtils.class)) {

            // when
            projectService.deleteDiagram(projectMock, diagramMock);

            // then
            verify(diagramTypeNoteService, times(1)).delete(eq(diagramMock));
            verify(regenerateDiagramsJobService, times(1)).delete(eq(diagramMock));
            verify(projectMock, times(1)).removeDiagram(eq(diagramMock));
            verify(repository, times(1)).save(eq(projectMock));
            fileIOUtilsMocked.verify(() -> FileIOUtils.deleteFileByAbsolutePath(any()));
        }
    }

    @Test
    void Should_ThrowDiagramViewerExceptionOnDeleteDiagram_When_DeleteFilesThrowsIOException() {

        // given
        Project projectMock = mock(Project.class);
        when(projectMock.getId()).thenReturn(new UUID(0, 0));
        Diagram diagramMock = mock(Diagram.class);
        when(diagramMock.getFileName()).thenReturn("diagramName.svg");

        doNothing().when(diagramTypeNoteService).delete(eq(diagramMock));
        doNothing().when(regenerateDiagramsJobService).delete(eq(diagramMock));
        doNothing().when(projectMock).removeDiagram(eq(diagramMock));
        when(repository.save(eq(projectMock))).thenReturn(projectMock);

        try(MockedStatic<FileIOUtils> fileIOUtilsMocked = mockStatic(FileIOUtils.class)) {

            fileIOUtilsMocked.when(() -> FileIOUtils.deleteFileByAbsolutePath(any())).thenThrow(IOException.class);

            // when
            assertThatThrownBy(() -> projectService.deleteDiagram(projectMock, diagramMock))
                .isInstanceOf(DiagramViewerException.class)
                .hasMessage("Couldn't finalize deleting diagram because some files couldn't be deleted from the filesystem.");

            // then
            verify(diagramTypeNoteService, times(1)).delete(eq(diagramMock));
            verify(regenerateDiagramsJobService, times(1)).delete(eq(diagramMock));
            verify(projectMock, times(1)).removeDiagram(eq(diagramMock));
            verify(repository, times(1)).save(eq(projectMock));
            fileIOUtilsMocked.verify(() -> FileIOUtils.deleteFileByAbsolutePath(any()));
        }
    }

    @Test
    void Should_DeleteDiagramDirectory() {

        // given
        Project projectMock = mock(Project.class);
        DiagramDirectory diagramDirectoryMock = mock(DiagramDirectory.class);

        doNothing().when(projectMock).removeDiagramDirectory(eq(diagramDirectoryMock));
        doNothing().when(diagramDirectoryMock).removeAllDiagrams();
        when(repository.save(eq(projectMock))).thenReturn(projectMock);

        // when
        projectService.deleteDiagramDirectory(projectMock, diagramDirectoryMock);

        // then
        verify(projectMock, times(1)).removeDiagramDirectory(eq(diagramDirectoryMock));
        verify(repository, times(1)).save(eq(projectMock));
        verify(diagramDirectoryMock, times(1)).removeAllDiagrams();
    }
}