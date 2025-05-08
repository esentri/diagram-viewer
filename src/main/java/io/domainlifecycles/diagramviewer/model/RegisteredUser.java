package io.domainlifecycles.diagramviewer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@Table(name = "RegisteredUser")
@ToString(exclude = "assignedProjects")
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class RegisteredUser extends User {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID apiKey;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "assignedRegisteredUsers")
    private Set<Project> assignedProjects;

    @Override
    public void addAssignedProject(Project project) {
        assignedProjects.add(project);
        this.assignedProjects = new HashSet<>(assignedProjects);
    }

    @Override
    public void removeAssignedProject(Project project) {
        assignedProjects.remove(project);
        this.assignedProjects = new HashSet<>(assignedProjects);
    }

    public boolean hasApiKey() {
        return apiKey != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegisteredUser registeredUser)) return false;
        return id != null && id.equals(registeredUser.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : super.hashCode();
    }
}
