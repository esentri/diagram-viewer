package io.domainlifecycles.diagramviewer.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.Data;

@Entity
@Data
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long projectId;
    private String projectNameFull;
    private String projectNameClean; // First part of project name split by special character like '.'
    private String absolutePathToTarget;

    @ElementCollection
    private List<String> boundedContextPackages;

    @OneToMany
    private List<Diagram> diagrams;
}
