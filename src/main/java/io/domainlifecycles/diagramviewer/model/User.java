package io.domainlifecycles.diagramviewer.model;

import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MappedSuperclass;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@MappedSuperclass
public abstract class User {

    private String emailAddress;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Project> assignedProjects;

    public void addAssignedProject(final Project project) {
        List<Project> currentProjects = new ArrayList<>(assignedProjects);
        currentProjects.add(project);
        assignedProjects = currentProjects;
    }
}
