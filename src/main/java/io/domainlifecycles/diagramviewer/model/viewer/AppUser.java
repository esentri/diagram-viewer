package io.domainlifecycles.diagramviewer.model.viewer;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@Table(name = "app_user")
@ToString(exclude = "assignedProjects")
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique=true, nullable = false)
    private String emailAddress;

    private String firstName;

    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private Set<UserIdentity> identities = new HashSet<>();

    private UUID apiKey;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "assignedUsers")
    private Set<Project> assignedProjects = new HashSet<>();

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant changedAt;

    public void addAssignedProject(Project project) {
        assignedProjects.add(project);
        this.assignedProjects = new HashSet<>(assignedProjects);
    }

    public void removeAssignedProject(Project project) {
        assignedProjects.remove(project);
        this.assignedProjects = new HashSet<>(assignedProjects);
    }

    public void addUserIdentity(UserIdentity userIdentity) {
        identities.add(userIdentity);
        this.identities = new HashSet<>(identities);
        userIdentity.setUser(this);
    }

    public boolean hasApiKey() {
        return apiKey != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppUser appUser)) return false;
        return id != null && id.equals(appUser.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : super.hashCode();
    }
}
