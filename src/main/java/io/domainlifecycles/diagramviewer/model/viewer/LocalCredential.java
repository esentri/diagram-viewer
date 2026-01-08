package io.domainlifecycles.diagramviewer.model.viewer;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "local_credential")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = "identity")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class LocalCredential {

    @Id
    private UUID id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_identity_id", nullable = false, foreignKey = @ForeignKey(name = "fk_credential_identity"))
    private UserIdentity identity;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant changedAt;
}