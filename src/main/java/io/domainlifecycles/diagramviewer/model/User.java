package io.domainlifecycles.diagramviewer.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@MappedSuperclass
public abstract class User {

    @Column(unique=true)
    private String emailAddress;
    private String fullName;

    @ManyToMany(fetch = FetchType.EAGER)
    @Builder.Default private List<Project> assignedProjects = new ArrayList<>();

    public void addAssignedProject(final Project project) {
        List<Project> currentProjects = new ArrayList<>(assignedProjects);
        currentProjects.add(project);
        assignedProjects = currentProjects;
    }

    public void removeAssignedProject(Project project) {
        List<Project> currentProjects = new ArrayList<>(assignedProjects);
        currentProjects.remove(project);
        assignedProjects = currentProjects;
    }
}
