package io.domainlifecycles.diagramviewer.model.viewer;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "user_identity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = "user")
public class UserIdentity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdentityProvider provider;

    /**
     * OKTA: OIDC sub
     * LOCAL: new UUID
     */
    @Column(nullable = false, updatable = false)
    private String externalSubject;

    @OneToOne(mappedBy = "identity", cascade = CascadeType.ALL, orphanRemoval = true)
    private LocalCredential localCredential;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant changedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserIdentity userIdentity)) return false;
        return id != null && id.equals(userIdentity.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : super.hashCode();
    }
}