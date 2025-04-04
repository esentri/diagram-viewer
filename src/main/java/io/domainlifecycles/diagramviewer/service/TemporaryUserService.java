package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.TemporaryUser;

public interface TemporaryUserService {

    boolean userKnown(final String userEmailAddress);
    TemporaryUser create(String userEmailAddress);
    TemporaryUser get(String userEmailAddress);
    void delete(TemporaryUser temporaryUser);
}
