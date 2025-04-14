package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.AuthenticatedUser;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.repository.AuthenticatedUserRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedUserServiceImpl implements AuthenticatedUserService {

    private final AuthenticatedUserRepository repository;

    public AuthenticatedUserServiceImpl(AuthenticatedUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean userKnown(final String userEmailAddress) {
        return repository.findByEmailAddress(userEmailAddress).isPresent();
    }

    @Override
    public AuthenticatedUser get(final String userEmailAddress) {
        return repository.getByEmailAddress(userEmailAddress);
    }

    @Override
    public AuthenticatedUser createUser(String userEmailAddress, String fullName) {
        final AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
            .emailAddress(userEmailAddress)
            .fullName(fullName)
            .build();

        return repository.save(authenticatedUser);
    }

    @Override
    public AuthenticatedUser createUser(String userEmailAddress, String fullName, List<Project> projects) {
        final AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
            .emailAddress(userEmailAddress)
            .fullName(fullName)
            .assignedProjects(projects)
            .build();

        return repository.save(authenticatedUser);
    }
}
