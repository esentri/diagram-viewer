package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.ProjectDomainMirror;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.UploadFileType;
import io.domainlifecycles.mirror.api.AggregateRootMirror;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ProjectDomainMirrorService {

    ProjectDomainMirror getByProjectId(final UUID projectId);

    List<DomainTypeMirror> getAllDomainTypeMirrorsWithoutEnumsAndIds(final UUID projectId);

    List<AggregateRootMirror> getAllAggregateRootMirrors(final UUID projectId);

    ProjectDomainMirror createOrUpdate(final Project project, Set<String> domainModelPackages, Path pathToFile, UploadFileType uploadFileType);

    ProjectDomainMirror createOrUpdate(final Project project, DomainMirror domainMirror);

    void delete(final UUID projectId);
}
