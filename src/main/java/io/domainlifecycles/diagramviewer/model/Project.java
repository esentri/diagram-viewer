package io.domainlifecycles.diagramviewer.model;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "PROJECT")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long projectId;
    private String projectNameFull;
    private String projectNameClean; // Project name escaping special characters like '.'

    @Column(unique=true)
    private String absolutePathToTarget;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> boundedContextPackages;

    @OneToMany(fetch = FetchType.EAGER)
    private List<Diagram> diagrams;

    public void addDiagram(Diagram diagram) {
        diagrams.add(diagram);
    }
}
