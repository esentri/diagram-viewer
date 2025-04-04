package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.TemporaryUser;
import io.domainlifecycles.diagramviewer.model.User;
import io.domainlifecycles.diagramviewer.repository.UserRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final TemporaryUserService temporaryUserService;

    public UserServiceImpl(UserRepository repository, TemporaryUserService temporaryUserService) {
        this.repository = repository;
        this.temporaryUserService = temporaryUserService;
    }

    @Override
    public boolean userKnown(final String userEmailAddress) {
        return repository.findByEmailAddress(userEmailAddress).isPresent();
    }

    @Override
    public User acknowledgeUserAuthentication(String userEmailAddress, String fullName) {
        if(userKnown(userEmailAddress)) return get(userEmailAddress);

        if(!temporaryUserService.userKnown(userEmailAddress)) {
            return addUser(userEmailAddress, fullName);
        }

        final TemporaryUser temporaryUser = temporaryUserService.get(userEmailAddress);
        return createNewUserAndRemoveTemporaryUser(temporaryUser);
    }

    @Override
    public void addProject(User user, Project project) {
        user.addAssignedProject(project);
        repository.save(user);
    }

    @Override
    public boolean checkAccess(String projectNameClean, User user) {
        if(user == null || user.getAssignedProjects() == null) return false;

        return user.getAssignedProjects().stream()
            .anyMatch(project -> Objects.equals(project.getProjectNameClean(), projectNameClean));
    }

    private User get(final String userEmailAddress) {
        return repository.getByEmailAddress(userEmailAddress);
    }

    private User createNewUserAndRemoveTemporaryUser(TemporaryUser temporaryUser) {
        temporaryUserService.delete(temporaryUser);
        return addUser(temporaryUser);
    }

    private User addUser(String userEmailAddress, String fullName) {
        final User user = User.builder()
            .emailAddress(userEmailAddress)
            .fullName(fullName)
            .assignedProjects(Collections.emptyList())
            .build();

        return repository.save(user);
    }

    private User addUser(TemporaryUser temporaryUser) {
        final User user = User.builder()
            .emailAddress(temporaryUser.getEmailAddress())
            .fullName(temporaryUser.getFullName())
            .assignedProjects(new ArrayList<>())
            .build();

        return repository.save(user);
    }
}
