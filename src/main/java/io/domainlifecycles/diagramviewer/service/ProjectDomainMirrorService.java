package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.ProjectDomainMirror;
import io.domainlifecycles.mirror.api.AggregateRootMirror;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ProjectDomainMirrorService {

    ProjectDomainMirror getByProjectId(final UUID projectId);

    List<DomainTypeMirror> getAllDomainTypeMirrors(final UUID projectId);

    List<AggregateRootMirror> getAllAggregateRootMirrors(final UUID projectId);

    void createOrUpdate(final UUID projectId, Path projectFilePath, Set<String> domainModelPackages);

    void createOrUpdate(final UUID projectId, DomainMirror domainMirror);

    void delete(final UUID projectId);
}
