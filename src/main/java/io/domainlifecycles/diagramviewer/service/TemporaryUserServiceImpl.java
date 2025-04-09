package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.AuthenticatedUser;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.TemporaryUser;
import io.domainlifecycles.diagramviewer.repository.TemporaryUserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TemporaryUserServiceImpl implements TemporaryUserService {

    private final TemporaryUserRepository repository;

    public TemporaryUserServiceImpl(TemporaryUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean userKnown(String userEmailAddress) {
        return repository.findByEmailAddress(userEmailAddress).isPresent();
    }

    @Override
    public TemporaryUser getOrCreate(String userEmailAddress) {
        if(userKnown(userEmailAddress)) return get(userEmailAddress);

        TemporaryUser temporaryUser = TemporaryUser.builder()
            .emailAddress(userEmailAddress)
            .assignedProjects(new ArrayList<>())
            .build();

        return repository.save(temporaryUser);
    }

    @Override
    public TemporaryUser get(String userEmailAddress) {
        return repository.findByEmailAddress(userEmailAddress).orElseThrow(() ->
            DiagramViewerException.fail(String.format("No Temporary User found with E-Mail address '%s'.", userEmailAddress)));
    }

    @Override
    public TemporaryUser addProject(TemporaryUser temporaryUser, Project project) {
        temporaryUser.addAssignedProject(project);
        return repository.save(temporaryUser);
    }

    @Override
    public TemporaryUser removeProject(TemporaryUser temporaryUser, Project project) {
        temporaryUser.removeAssignedProject(project);
        return repository.save(temporaryUser);
    }

    @Override
    public void delete(TemporaryUser temporaryUser) {
        repository.delete(temporaryUser);
    }
}
