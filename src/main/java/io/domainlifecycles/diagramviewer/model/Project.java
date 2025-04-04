package io.domainlifecycles.diagramviewer.model;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

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
    @Builder.Default private List<String> boundedContextPackages = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER)
    @Builder.Default private List<Diagram> diagrams = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @Builder.Default private List<AuthenticatedUser> assignedAuthenticatedUsers = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @Builder.Default private List<TemporaryUser> assignedTemporaryUsers = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    private AuthenticatedUser creator;

    public void addDiagram(Diagram diagram) {
        diagrams.add(diagram);
    }

    public void unassignUser(User user) {
        if(user instanceof AuthenticatedUser) {
            List<AuthenticatedUser> currentAssignedAuthenticatedUsers = new ArrayList<>(assignedAuthenticatedUsers);
            currentAssignedAuthenticatedUsers.remove((AuthenticatedUser) user);
            assignedAuthenticatedUsers = currentAssignedAuthenticatedUsers;
        } else {
            List<TemporaryUser> currentAssignedTemporaryUsers = new ArrayList<>(assignedTemporaryUsers);
            currentAssignedTemporaryUsers.remove((TemporaryUser) user);
            assignedTemporaryUsers = currentAssignedTemporaryUsers;
        }
    }

    public void assignUser(User user) {
        if(user instanceof AuthenticatedUser) {
            List<AuthenticatedUser> currentAssignedAuthenticatedUsers = new ArrayList<>(assignedAuthenticatedUsers);
            currentAssignedAuthenticatedUsers.add((AuthenticatedUser) user);
            assignedAuthenticatedUsers = currentAssignedAuthenticatedUsers;
        } else {
            List<TemporaryUser> currentAssignedTemporaryUsers = new ArrayList<>(assignedTemporaryUsers);
            currentAssignedTemporaryUsers.add((TemporaryUser) user);
            assignedTemporaryUsers = currentAssignedTemporaryUsers;
        }
    }
}
