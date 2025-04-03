package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.TemporaryUser;
import io.domainlifecycles.diagramviewer.model.User;
import io.domainlifecycles.diagramviewer.repository.UserRepository;
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
            .build();

        return repository.save(user);
    }

    private User addUser(TemporaryUser temporaryUser) {
        final User user = User.builder()
            .emailAddress(temporaryUser.getEmailAddress())
            .fullName(temporaryUser.getFullName())
            .assignedProjects(temporaryUser.getAssignedProjects())
            .build();

        return repository.save(user);
    }
}
