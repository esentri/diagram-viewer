package io.domainlifecycles.diagramviewer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An Instance of a Temporary User is created, when a Project admin adds a new email-address to the Project members and
 * the given email-address is not known to the system. The temporary user remains in the database for as long as he signs up
 * for the first time, then this instance will be deleted and instead a new {@link User} created.
 * Temporary Users allow the project admin to give users access to projects, although they haven't signed up yet.
 *
 * @author leonvoellinger
 */
@Entity
@Data
@Table(name = "TEMPORARY_USER")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TemporaryUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long temporaryUserId;
    private String emailAddress;
    private String fullName;

    @OneToMany(fetch = FetchType.EAGER)
    private List<Project> assignedProjects;

    public void addProject(Project project) {
        assignedProjects.add(project);
    }
}
