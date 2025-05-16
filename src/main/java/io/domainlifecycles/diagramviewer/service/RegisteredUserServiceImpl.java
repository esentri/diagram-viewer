package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.viewer.RegisteredUser;
import io.domainlifecycles.diagramviewer.repository.RegisteredUserRepository;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RegisteredUserServiceImpl implements RegisteredUserService {

    private final RegisteredUserRepository repository;

    public RegisteredUserServiceImpl(RegisteredUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean userKnown(final String userEmailAddress) {
        return repository.findByEmailAddress(userEmailAddress).isPresent();
    }

    @Override
    public RegisteredUser get(final String userEmailAddress) {
        return repository.getByEmailAddress(userEmailAddress);
    }

    @Override
    public Optional<RegisteredUser> findByApiKey(String apiKey) {
        return repository.findByApiKey(UUID.fromString(apiKey));
    }

    @Override
    public RegisteredUser createUser(String userEmailAddress, String fullName) {
        final RegisteredUser registeredUser = RegisteredUser.builder()
            .emailAddress(userEmailAddress)
            .fullName(fullName)
            .assignedProjects(new HashSet<>())
            .build();

        return repository.save(registeredUser);
    }

    @Override
    public void generateApiKeyForUser(RegisteredUser registeredUser) {
        registeredUser.setApiKey(UUID.randomUUID());
        repository.save(registeredUser);
    }
}
