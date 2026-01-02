package io.domainlifecycles.diagramviewer.service.project;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramDirectory;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import io.domainlifecycles.diagramviewer.repository.ProjectRepository;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.service.DiagramTypeNoteService;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.diagramviewer.service.ProjectServiceImpl;
import io.domainlifecycles.diagramviewer.service.RegenerateDiagramsJobService;
import io.domainlifecycles.diagramviewer.service.AppUserService;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.UploadFileType;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.mirror.api.DomainMirror;
import java.io.IOException;
import java.nio.file.Path;
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
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
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
    AppUserService appUserService;

    @Mock
    SessionStorage sessionStorage;

    @Mock
    ProjectRepository repository;

    ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectServiceImpl("/tmp/diagrams", diagramService, diagramTypeNoteService,
            regenerateDiagramsJobService, appUserService, sessionStorage, repository);
    }

    @Test
    void Should_GetAllProjectsAssignedToUser() {

        // given
        AppUser appUserMock = mock(AppUser.class);
        when(appUserMock.getId()).thenReturn(new UUID(0, 0));

        AppUser anotherAppUserMock = mock(AppUser.class);
        when(anotherAppUserMock.getId()).thenReturn(new UUID(0, 0));

        Project projectAssignedToRegisteredUserMock = mock(Project.class);
        when(projectAssignedToRegisteredUserMock.getAssignedUsers()).thenReturn(Set.of(appUserMock));

        Project projectAssignedToAnotherRegisteredUserMock = mock(Project.class);
        when(projectAssignedToAnotherRegisteredUserMock.getAssignedUsers()).thenReturn(
            Set.of(anotherAppUserMock));

        when(repository.findAll()).thenReturn(
            List.of(projectAssignedToRegisteredUserMock, projectAssignedToAnotherRegisteredUserMock));

        // when
        Stream<Project> result = projectService.getAll(appUserMock);

        // then
        assertThat(result.toList().get(0)).isEqualTo(projectAssignedToRegisteredUserMock);
        verify(repository, times(1)).findAll();
    }

    @Test
    void Should_FindProjectById_When_ProjectIsPresent() {

        // given
        UUID projectId = UUID.randomUUID();
        Project project = mock(Project.class);

        when(repository.findById(projectId)).thenReturn(Optional.of(project));

        // when
        Optional<Project> result = projectService.findById(projectId);

        // then
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get()).isEqualTo(project);
        verify(repository, times(1)).findById(projectId);
    }

    @Test
    void Should_GetProjectByName_When_ProjectExists() {

        // given
        String projectName = "projectName";

        Project projectMock = mock(Project.class);

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

        AppUser appUserMock = mock(AppUser.class);
        when(appUserMock.getId()).thenReturn(new UUID(0, 0));

        Project projectMock = mock(Project.class);
        when(projectMock.getCreator()).thenReturn(appUserMock);

        doNothing().when(sessionStorage).createOrUpdate(eq(projectMock), eq(domainModelPackages), eq(pathToFile),
            eq(uploadFileType));

        // when
        Project result = projectService.updateDomainMirror(projectMock, domainModelPackages, appUserMock,
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
        AppUser appUserMock = mock(AppUser.class);
        when(appUserMock.getId()).thenReturn(new UUID(0, 0));
        when(appUserMock.getEmailAddress()).thenReturn(userMailAddress);

        AppUser anotherAppUserMock = mock(AppUser.class);

        String projectName = "projectName";
        Project projectMock = mock(Project.class);
        when(projectMock.getName()).thenReturn(projectName);
        when(projectMock.getCreator()).thenReturn(anotherAppUserMock);

        // when
        assertThatThrownBy(() -> projectService.updateDomainMirror(projectMock, domainModelPackages, appUserMock,
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
        AppUser appUserMock = mock(AppUser.class);

        when(repository.findByName(eq(projectName))).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(mock(Project.class));
        doNothing().when(sessionStorage).createOrUpdate(any(Project.class), eq(domainModelPackages), eq(pathToFile),
            eq(uploadFileType));

        // when
        projectService.create(projectName, domainModelPackages, appUserMock, pathToFile, uploadFileType);

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
        AppUser appUserMock = mock(AppUser.class);

        when(repository.findByName(eq(projectName))).thenReturn(Optional.of(mock(Project.class)));

        // when
        assertThatThrownBy(() -> projectService.create(projectName, domainModelPackages, appUserMock, pathToFile,
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
        AppUser appUserMock = mock(AppUser.class);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(appUserMock, null));

        String projectName = "projectName";
        Project projectMock = mock(Project.class);
        when(projectMock.getAssignedUsers()).thenReturn(Set.of(appUserMock));

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
        AppUser appUserMock = mock(AppUser.class);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(appUserMock, null));

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
        AppUser appUserMock = mock(AppUser.class);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(appUserMock, null));

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
        AppUser appUserMock = mock(AppUser.class);

        String projectName = "testProjectName";
        Project projectMock = mock(Project.class);
        when(projectMock.getName()).thenReturn(projectName);
        when(projectMock.getCreator()).thenReturn(appUserMock);

        when(repository.findByName(eq(projectName))).thenReturn(Optional.empty());

        // when
        projectService.rename(projectMock, appUserMock, "new-Project.Name");

        // then
        verify(repository, times(1)).findByName(eq(projectName));
        verify(projectMock, times(1)).setName("new_Project_Name");
        verify(repository, times(1)).save(eq(projectMock));
    }

    @Test
    void Should_ThrowDiagramViewerExceptionOnRenameProject_NameIsNull() {

        // given
        AppUser appUserMock = mock(AppUser.class);

        Project projectMock = mock(Project.class);
        when(projectMock.getCreator()).thenReturn(appUserMock);

        // when
        assertThatThrownBy(() -> projectService.rename(projectMock, appUserMock, null))
            .isInstanceOf(DiagramViewerException.class)
                .hasMessage("Project name may not be empty.");

        // then
        verify(repository, never()).save(any());
    }

    @Test
    void Should_ThrowDiagramViewerExceptionOnRenameProject_NameIsBlank() {

        // given
        AppUser appUserMock = mock(AppUser.class);

        Project projectMock = mock(Project.class);
        when(projectMock.getCreator()).thenReturn(appUserMock);

        // when
        assertThatThrownBy(() -> projectService.rename(projectMock, appUserMock, " "))
            .isInstanceOf(DiagramViewerException.class)
            .hasMessage("Project name may not be empty.");

        // then
        verify(repository, never()).save(any());
    }

    @Test
    void Should_AssignUserToProject_When_UserIsNotAlreadyAssignedAndUserKnown() {

        // given
        String emailAddress = "max.mustermann@gmail.com";

        AppUser appUserMock = mock(AppUser.class);
        when(appUserMock.getId()).thenReturn(new UUID(0, 0));

        AppUser anotherAppUserMock = mock(AppUser.class);
        when(anotherAppUserMock.getId()).thenReturn(new UUID(1, 1));
        when(anotherAppUserMock.getEmailAddress()).thenReturn("another.mail@gmail.com");

        Project projectMock = mock(Project.class);
        when(projectMock.getAssignedUsers()).thenReturn(Set.of(anotherAppUserMock));

        when(appUserService.userKnownAndActive(eq(emailAddress))).thenReturn(true);
        when(appUserService.get(eq(emailAddress))).thenReturn(appUserMock);
        when(repository.save(eq(projectMock))).thenReturn(projectMock);

        // when
        projectService.assignUser(projectMock, emailAddress);

        // then
        verify(appUserService, times(1)).userKnownAndActive(eq(emailAddress));
        verify(appUserService, times(1)).get(eq(emailAddress));
        verify(projectMock, times(1)).assignUser(any());
        verify(repository, times(1)).save(projectMock);
    }

    @Test
    void Should_NotAssignUserToProject_When_UserIsAlreadyAssigned() {

        // given
        String emailAddress = "max.mustermann@gmail.com";

        AppUser appUserMock = mock(AppUser.class);
        when(appUserMock.getEmailAddress()).thenReturn(emailAddress);

        Project projectMock = mock(Project.class);
        when(projectMock.getAssignedUsers()).thenReturn(Set.of(appUserMock));

        // when
        projectService.assignUser(projectMock, emailAddress);

        // then
        verifyNoInteractions(appUserService);
    }

    /*@Test
    void Should_AssignUserToProject_When_UserIsNotAlreadyAssignedAndUserNotKnown() {

        // given
        String emailAddress = "max.mustermann@gmail.com";

        InvitedUser invitedUserMock = mock(InvitedUser.class);

        AppUser anotherAppUserMock = mock(AppUser.class);
        when(anotherAppUserMock.getEmailAddress()).thenReturn("another.mail@gmail.com");

        Project projectMock = mock(Project.class);
        when(projectMock.getAssignedUsers()).thenReturn(Set.of(anotherAppUserMock));

        when(appUserService.userKnownAndActive(eq(emailAddress))).thenReturn(false);
        when(invitedUserService.getOrCreate(eq(emailAddress))).thenReturn(invitedUserMock);
        when(repository.save(eq(projectMock))).thenReturn(projectMock);

        // when
        projectService.assignUser(projectMock, emailAddress);

        // then
        verify(appUserService, times(1)).userKnownAndActive(eq(emailAddress));
        verify(invitedUserService, times(1)).getOrCreate(eq(emailAddress));
        verify(projectMock, times(1)).assignUser(any());
        verify(repository, times(1)).save(projectMock);
    }*/

    @Test
    void Should_ThrowDiagramViewerExceptionOnAssignUser_When_RegisteredUserIsAlreadyAssigned() {

        // given
        AppUser appUserMock = mock(AppUser.class);
        String emailAddress = "max.mustermann@gmail.com";
        when(appUserMock.getEmailAddress()).thenReturn(emailAddress);

        Project projectMock = mock(Project.class);
        when(projectMock.getAssignedUsers()).thenReturn(Set.of(appUserMock));

        // when
        assertThatThrownBy(() -> projectService.assignUser(projectMock, appUserMock))
            .hasMessage("User '" + emailAddress + "' is already assigned to project.");

        // then
        verifyNoInteractions(repository);
    }

    /*@Test
    void Should_ThrowDiagramViewerExceptionOnAssignUser_When_InvitedUserIsAlreadyAssigned() {

        // given
        InvitedUser invitedUserMock = mock(InvitedUser.class);
        String emailAddress = "max.mustermann@gmail.com";
        when(invitedUserMock.getEmailAddress()).thenReturn(emailAddress);

        Project projectMock = mock(Project.class);
        when(projectMock.getAssignedInvitedUsers()).thenReturn(Set.of(invitedUserMock));

        // when
        assertThatThrownBy(() -> projectService.assignUser(projectMock, invitedUserMock))
            .hasMessage("User '" + emailAddress + "' is already assigned to project.");

        // then
        verifyNoInteractions(repository);
    }*/

    @Test
    void Should_NotUnassignUser_When_UserIsCreator() {

        // given
        AppUser appUserMock = mock(AppUser.class);

        Project projectMock = mock(Project.class);
        when(projectMock.getCreator()).thenReturn(appUserMock);

        // when
        projectService.unassignUser(projectMock, appUserMock);

        // then
        verifyNoInteractions(repository);
    }

    @Test
    void Should_UnassignUser_When_UserIsNotCreator() {

        // given
        AppUser appUserMock = mock(AppUser.class);

        AppUser anotherAppUserMock = mock(AppUser.class);
        when(anotherAppUserMock.getId()).thenReturn(new UUID(0, 0));

        Project projectMock = mock(Project.class);
        when(projectMock.getCreator()).thenReturn(anotherAppUserMock);

        doNothing().when(projectMock).unassignUser(eq(appUserMock));
        when(repository.save(eq(projectMock))).thenReturn(projectMock);

        // when
        projectService.unassignUser(projectMock, appUserMock);

        // then
        verify(projectMock, times(1)).unassignUser(eq(appUserMock));
        verify(repository, times(1)).save(eq(projectMock));
    }

    /*@Test
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
    void Should_UnassignButNotDeleteUser_When_UserIsInvitedUserButAssignedToOtherProject() {

        // given
        InvitedUser invitedUserMock = mock(InvitedUser.class);

        Project projectMock = mock(Project.class);

        when(repository.save(eq(projectMock))).thenReturn(projectMock);
        when(invitedUserService.checkForRemoval(eq(invitedUserMock))).thenReturn(false);

        // when
        projectService.unassignUser(projectMock, invitedUserMock);

        // then
        verify(projectMock, times(1)).unassignUser(eq(invitedUserMock));
        verify(repository, times(1)).save(eq(projectMock));
        verify(invitedUserService, times(1)).checkForRemoval(eq(invitedUserMock));
        verify(invitedUserService, never()).delete(any());
    }*/

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
        when(diagramMock.getName()).thenReturn("diagramName.svg");

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
        when(diagramMock.getName()).thenReturn("diagramName.svg");

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