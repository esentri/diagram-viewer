package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.InvitedUser;
import io.domainlifecycles.diagramviewer.repository.InvitedUserRepository;
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
        InvitedUser fetchedInvitedUser = get(invitedUser.getEmailAddress());
        fetchedInvitedUser.getAssignedProjects().forEach(project -> project.unassignUser(invitedUser));
        repository.delete(fetchedInvitedUser);
    }

    @Override
    public boolean checkForRemoval(InvitedUser invitedUser) {
        InvitedUser fetchedInvitedUser = repository.findById(invitedUser.getId()).orElseThrow();
        return fetchedInvitedUser.getAssignedProjects().isEmpty();
    }
}
