package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.TemporaryUser;
import io.domainlifecycles.diagramviewer.repository.TemporaryUserRepository;
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
    public TemporaryUser create(String userEmailAddress) {
        TemporaryUser temporaryUser = TemporaryUser.builder()
            .emailAddress(userEmailAddress)
            .build();

        return repository.save(temporaryUser);
    }

    @Override
    public TemporaryUser get(String userEmailAddress) {
        return repository.findByEmailAddress(userEmailAddress).orElseThrow(() ->
            DiagramViewerException.fail(String.format("No Temporary User found with E-Mail address '%s'.", userEmailAddress)));
    }

    @Override
    public void delete(TemporaryUser temporaryUser) {
        repository.delete(temporaryUser);
    }
}
