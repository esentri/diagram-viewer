package io.domainlifecycles.diagramviewer.rest.api.model;

import io.domainlifecycles.mirror.api.DomainMirror;
import java.util.Set;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
public record DomainMirrorUploadDto(DomainMirror domainMirror, Set<String> domainModelPackages) { }
