package io.domainlifecycles.diagramviewer.model.task;

import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "RegenerateDiagramsJob")
@Data
@ToString(exclude = "diagram")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegenerateDiagramsJob {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="diagram_id", nullable=false)
    private Diagram diagram;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant changedAt;
}
