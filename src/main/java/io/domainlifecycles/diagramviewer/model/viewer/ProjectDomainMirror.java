package io.domainlifecycles.diagramviewer.model.viewer;

import io.domainlifecycles.diagramviewer.model.converter.DomainModelConverter;
import io.domainlifecycles.mirror.api.DomainMirror;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@Table(name = "project_domain_mirror")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDomainMirror {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID projectId;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = DomainModelConverter.class)
    private DomainMirror domainMirror;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant changedAt;

}
