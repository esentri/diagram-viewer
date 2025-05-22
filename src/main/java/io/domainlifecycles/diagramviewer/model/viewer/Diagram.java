package io.domainlifecycles.diagramviewer.model.viewer;

import io.domainlifecycles.diagramviewer.rest.kroki.FileType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "Diagram")
@Data
@ToString(exclude = "project")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Diagram {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private FileType fileType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="project_id", nullable=false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="diagram_directory_id")
    private DiagramDirectory diagramDirectory;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Builder.Default
    private DiagramStylingConfiguration diagramStylingConfiguration = new DiagramStylingConfiguration();

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Builder.Default
    private DomainModelVisibility domainModelVisibility = new DomainModelVisibility(null, null, null, null, null, null, null);

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant changedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Diagram diagram)) return false;
        return id != null && id.equals(diagram.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : super.hashCode();
    }
}
