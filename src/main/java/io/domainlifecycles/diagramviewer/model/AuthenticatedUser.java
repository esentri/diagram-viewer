package io.domainlifecycles.diagramviewer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "AuthenticatedUser")
@ToString(exclude = "assignedProjects")
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class AuthenticatedUser extends User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "assignedAuthenticatedUsers")
    @Builder.Default
    private List<Project> assignedProjects = new ArrayList<>();

    @Override
    public void addAssignedProject(Project project) {
        assignedProjects.add(project);
    }

    @Override
    public void removeAssignedProject(Project project) {
        assignedProjects.removeIf(p -> Objects.equals(p.getId(), project.getId()));
    }
}
