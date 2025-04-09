package io.domainlifecycles.diagramviewer.model;

import io.domainlifecycles.diagramviewer.kroki.FileType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "DIAGRAM")
@Data
@ToString(exclude = "project")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Diagram {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long diagramId;

    private String fileName;
    private FileType fileType;
    private String fullAbsoluteLocationPath;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private Project project;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Builder.Default private DiagramStylingConfiguration diagramStylingConfiguration = new DiagramStylingConfiguration();

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Builder.Default private DomainModelVisibility domainModelVisibility = new DomainModelVisibility(null, null);
}
