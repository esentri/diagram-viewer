package io.domainlifecycles.diagramviewer.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "DiagramDirectory")
@Data
@ToString(exclude = {"project", "diagrams"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiagramDirectory {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="project_id", nullable=false)
    private Project project;

    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "diagramDirectory")
    private Set<Diagram> diagrams;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant changedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiagramDirectory diagramDirectory)) return false;
        return id != null && id.equals(diagramDirectory.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : super.hashCode();
    }

    public void addDiagram(Diagram diagram) {
        diagrams.add(diagram);
        diagram.setDiagramDirectory(this);
    }
}
