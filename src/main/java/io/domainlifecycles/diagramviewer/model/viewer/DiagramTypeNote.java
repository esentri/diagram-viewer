package io.domainlifecycles.diagramviewer.model.viewer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@Table(name = "DiagramTypeNote", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"notes", "diagram_id"})
})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiagramTypeNote {

    public static final int NOTES_MAX_LENGTH = 4000;

    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true)
    private String domainTypeMirrorName;

    @Column(length = NOTES_MAX_LENGTH)
    private String notes;

    @ManyToOne
    @JoinColumn(name="diagram_id", nullable=false)
    private Diagram diagram;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant changedAt;
}
