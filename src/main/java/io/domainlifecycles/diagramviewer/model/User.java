package io.domainlifecycles.diagramviewer.model;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MappedSuperclass;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
        assignedProjects.add(project);
    }

    public void removeAssignedProject(Project project) {
        assignedProjects.removeIf(p -> Objects.equals(p.getId(), project.getId()));
    }
}
