package io.domainlifecycles.diagramviewer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity
@Data
public class Diagram {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long diagramId;

    private String fileName;
    private String fullAbsoluteLocationPath;
    @OneToOne
    private DiagramStylingConfiguration diagramStylingConfiguration;
    @OneToOne
    private DomainModelVisibility domainModelVisibility = new DomainModelVisibility(null, null);
}
