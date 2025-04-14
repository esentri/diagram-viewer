package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.AuthenticatedUser;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.TemporaryUser;
import io.domainlifecycles.diagramviewer.repository.UserRepository;
import java.util.ArrayList;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedUserServiceImpl implements AuthenticatedUserService {

    private final UserRepository repository;
    private final TemporaryUserService temporaryUserService;

    public AuthenticatedUserServiceImpl(UserRepository repository, TemporaryUserService temporaryUserService) {
        this.repository = repository;
        this.temporaryUserService = temporaryUserService;
    }

    @Override
    public boolean userKnown(final String userEmailAddress) {
        return repository.findByEmailAddress(userEmailAddress).isPresent();
    }

    @Override
    public AuthenticatedUser acknowledgeUserAuthentication(String userEmailAddress, String fullName) {
        if(userKnown(userEmailAddress)) return get(userEmailAddress);

        if(!temporaryUserService.userKnown(userEmailAddress)) {
            return addUser(userEmailAddress, fullName);
        }

        final TemporaryUser temporaryUser = temporaryUserService.get(userEmailAddress);
        return createNewUserAndRemoveTemporaryUser(temporaryUser, fullName);
    }

    @Override
    public AuthenticatedUser addProject(AuthenticatedUser authenticatedUser, Project project) {
        authenticatedUser.addAssignedProject(project);
        return repository.save(authenticatedUser);
    }

    @Override
    public AuthenticatedUser removeProject(AuthenticatedUser authenticatedUser, Project project) {
        authenticatedUser.removeAssignedProject(project);
        return repository.save(authenticatedUser);
    }

    @Override
    public boolean checkAccess(String projectNameClean, AuthenticatedUser authenticatedUser) {
        if(authenticatedUser == null || authenticatedUser.getAssignedProjects() == null) return false;

        return authenticatedUser.getAssignedProjects().stream()
            .anyMatch(project -> Objects.equals(project.getProjectNameClean(), projectNameClean));
    }

    @Override
    public AuthenticatedUser get(final String userEmailAddress) {
        return repository.getByEmailAddress(userEmailAddress);
    }

    private AuthenticatedUser createNewUserAndRemoveTemporaryUser(TemporaryUser temporaryUser, String fullName) {
        temporaryUserService.delete(temporaryUser);
        return addUser(temporaryUser.getEmailAddress(), fullName);
    }

    private AuthenticatedUser addUser(String userEmailAddress, String fullName) {
        final AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
            .emailAddress(userEmailAddress)
            .fullName(fullName)
            .assignedProjects(new ArrayList<>())
            .build();

        return repository.save(authenticatedUser);
    }
}
