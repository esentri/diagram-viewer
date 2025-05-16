package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.viewer.InvitedUser;

public interface InvitedUserService {

    boolean userKnown(final String userEmailAddress);
    InvitedUser getOrCreate(String userEmailAddress);
    InvitedUser get(String userEmailAddress);
    void delete(InvitedUser invitedUser);
    boolean checkForRemoval(InvitedUser invitedUser);
}
