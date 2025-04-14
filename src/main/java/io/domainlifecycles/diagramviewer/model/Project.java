package io.domainlifecycles.diagramviewer.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Data
@Table(name = "Project")
@ToString(exclude = "diagrams")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String projectNameFull;

    @Column(nullable = false, unique = true)
    private String projectNameClean; // Project name escaping special characters like '.'

    private String displayName;

    @Column(unique=true)
    private String absolutePathToTarget;

    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default private List<String> boundedContextPackages = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "project")
    @Builder.Default
    private List<Diagram> diagrams = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "project_authenticated_users", joinColumns = @JoinColumn(name = "project_id"), inverseJoinColumns = @JoinColumn(name = "authenticated_user_id"))
    @Builder.Default
    private List<AuthenticatedUser> assignedAuthenticatedUsers = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "project_temporary_users", joinColumns = @JoinColumn(name = "project_id"), inverseJoinColumns = @JoinColumn(name = "temporary_user_id"))
    @Builder.Default
    private List<TemporaryUser> assignedTemporaryUsers = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="authenticated_user_id", nullable=false)
    private AuthenticatedUser creator;

    @CreationTimestamp
    private Instant createdAt;

    public void unassignUser(User user) {
        if(user instanceof AuthenticatedUser) {
            assignedAuthenticatedUsers.removeIf(u -> Objects.equals(((AuthenticatedUser) user).getId(), u.getId()));
        } else {
            assignedTemporaryUsers.removeIf(u -> Objects.equals(((TemporaryUser) user).getId(), u.getId()));
        }
        user.removeAssignedProject(this);
    }

    public void assignUser(User user) {
        if(user instanceof AuthenticatedUser) {
            assignedAuthenticatedUsers.add((AuthenticatedUser) user);
        } else {
            assignedTemporaryUsers.add((TemporaryUser) user);
        }
        user.addAssignedProject(this);
    }

    public void unassignAllUsers() {
        new HashSet<>(assignedAuthenticatedUsers).forEach(this::unassignUser);
        new HashSet<>(assignedTemporaryUsers).forEach(this::unassignUser);
    }
}
