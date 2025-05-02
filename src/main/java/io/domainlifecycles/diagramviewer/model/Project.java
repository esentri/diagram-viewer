package io.domainlifecycles.diagramviewer.model;

import io.domainlifecycles.diagramviewer.model.converter.DomainModelConverter;
import io.domainlifecycles.mirror.api.DomainModel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
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
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "project")
    private Set<Diagram> diagrams;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = DomainModelConverter.class)
    private DomainModel domainModel;

    @Column(nullable = false, updatable = false)
    private boolean apiUpload;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "project_assigned_registered_users", joinColumns = @JoinColumn(name = "project_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<RegisteredUser> assignedRegisteredUsers;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "project_assigned_invited_users", joinColumns = @JoinColumn(name = "project_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<InvitedUser> assignedInvitedUsers;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="creator_user_id", nullable=false)
    private RegisteredUser creator;

    @CreationTimestamp
    private Instant createdAt;

    public void unassignUser(User user) {
        if(user instanceof RegisteredUser) {
            assignedRegisteredUsers.remove((RegisteredUser) user);
            this.assignedRegisteredUsers = new HashSet<>(assignedRegisteredUsers);
        } else {
            assignedInvitedUsers.remove((InvitedUser) user);
            this.assignedInvitedUsers = new HashSet<>(assignedInvitedUsers);
        }
        user.removeAssignedProject(this);
    }

    public void assignUser(User user) {
        if(user instanceof RegisteredUser) {
            assignedRegisteredUsers.add((RegisteredUser) user);
            this.assignedRegisteredUsers = new HashSet<>(assignedRegisteredUsers);
        } else {
            assignedInvitedUsers.add((InvitedUser) user);
            this.assignedInvitedUsers = new HashSet<>(assignedInvitedUsers);
        }
        user.addAssignedProject(this);
    }

    public void unassignAllUsers() {
        new HashSet<>(assignedRegisteredUsers).forEach(this::unassignUser);
        new HashSet<>(assignedInvitedUsers).forEach(this::unassignUser);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project project)) return false;
        return id != null && id.equals(project.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : super.hashCode();
    }
}
