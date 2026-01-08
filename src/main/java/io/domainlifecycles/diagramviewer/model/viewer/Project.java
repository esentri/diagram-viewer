package io.domainlifecycles.diagramviewer.model.viewer;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@Table(name = "project")
@ToString(exclude = "diagrams")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "project")
    private Set<Diagram> diagrams;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "project")
    private Set<DiagramDirectory> diagramDirectories;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> domainModelPackages;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "project_assigned_users", joinColumns = @JoinColumn(name = "project_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<AppUser> assignedUsers;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="creator_user_id", nullable=false)
    private AppUser creator;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant changedAt;

    public void unassignUser(AppUser user) {
        if(user != null) {
            assignedUsers.remove(user);
            this.assignedUsers = new HashSet<>(assignedUsers);
            user.removeAssignedProject(this);
        }
    }

    public void assignUser(AppUser user) {
        if(user != null) {
            assignedUsers.add(user);
            this.assignedUsers = new HashSet<>(assignedUsers);
            user.addAssignedProject(this);
        }
    }

    public void unassignAllUsers() {
        new HashSet<>(assignedUsers).forEach(this::unassignUser);
    }

    public void addDiagram(Diagram diagram) {
        diagrams.add(diagram);
    }

    public void removeDiagram(Diagram diagram) {
        diagrams.remove(diagram);
        diagrams = new HashSet<>(diagrams);
    }

    public void addDiagramDirectory(DiagramDirectory diagramDirectory) {
        diagramDirectories.add(diagramDirectory);
        diagramDirectory.setProject(this);
    }

    public void removeDiagramDirectory(DiagramDirectory diagramDirectory) {
        diagramDirectories.remove(diagramDirectory);
        diagramDirectories = new HashSet<>(diagramDirectories);
        diagramDirectory.setProject(null);
    }

    public Set<Diagram> getDiagramsWithoutDirectory() {
        return diagrams.stream()
            .filter(diagram -> diagram.getDiagramDirectory() == null)
            .collect(Collectors.toSet());
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

    public Instant getLatestChangeInstant() {
        if(changedAt == null) {
            return createdAt;
        }
        return changedAt;
    }
}
