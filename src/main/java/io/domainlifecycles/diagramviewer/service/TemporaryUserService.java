package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.TemporaryUser;

public interface TemporaryUserService {

    boolean userKnown(final String userEmailAddress);
    TemporaryUser getOrCreate(String userEmailAddress);
    TemporaryUser get(String userEmailAddress);
    void delete(TemporaryUser temporaryUser);
    boolean checkForRemoval(TemporaryUser temporaryUser);
}
