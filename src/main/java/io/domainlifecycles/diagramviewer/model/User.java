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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "SERVICE_USER")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long userId;
    private String fullName;
    private String emailAddress;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Project> assignedProjects = new ArrayList<>();

    public void addAssignedProject(Project project) {
        List<Project> currentProjects = new ArrayList<>(assignedProjects);
        currentProjects.add(project);
        assignedProjects = currentProjects;
    }
}
