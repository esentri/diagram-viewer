package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.InvitedUser;
import io.domainlifecycles.diagramviewer.repository.InvitedUserRepository;
import java.util.HashSet;
import org.springframework.stereotype.Service;

@Service
public class InvitedUserServiceImpl implements InvitedUserService {

    private final InvitedUserRepository repository;

    public InvitedUserServiceImpl(InvitedUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean userKnown(String userEmailAddress) {
        return repository.findByEmailAddress(userEmailAddress).isPresent();
    }

    @Override
    public InvitedUser getOrCreate(String userEmailAddress) {
        if(userKnown(userEmailAddress)) return get(userEmailAddress);

        InvitedUser invitedUser = InvitedUser.builder()
            .emailAddress(userEmailAddress)
            .assignedProjects(new HashSet<>())
            .build();

        return repository.save(invitedUser);
    }

    @Override
    public InvitedUser get(String userEmailAddress) {
        return repository.findByEmailAddress(userEmailAddress).orElseThrow(() ->
            DiagramViewerException.fail(String.format("No Invited-User found with E-Mail address '%s'.", userEmailAddress)));
    }

    @Override
    public void delete(InvitedUser invitedUser) {
        invitedUser.getAssignedProjects().forEach(project -> project.unassignUser(invitedUser));
        repository.delete(invitedUser);
    }

    @Override
    public boolean checkForRemoval(InvitedUser invitedUser) {
        return invitedUser.getAssignedProjects().isEmpty();
    }
}
