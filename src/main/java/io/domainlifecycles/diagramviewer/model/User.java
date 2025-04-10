package io.domainlifecycles.diagramviewer.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@MappedSuperclass
public abstract class User {

    @Column(unique=true)
    private String emailAddress;
    private String fullName;

    @CreationTimestamp
    private Instant createdAt;

    public abstract void addAssignedProject(final Project project);
    public abstract void removeAssignedProject(Project project);
}
