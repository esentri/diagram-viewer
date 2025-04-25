package io.domainlifecycles.diagramviewer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * An Instance of a InvitedUser is created, when a Project admin adds a new email-address to the Project members and
 * the given email-address is not known to the system. The invited user remains in the database for as long as he signs up
 * for the first time, then this instance will be deleted and instead a new {@link RegisteredUser} created.
 * Invited Users allow the project admin to give users access to projects, although they haven't signed up yet.
 *
 * @author leonvoellinger
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "InvitedUser")
@ToString(exclude = "assignedProjects")
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class InvitedUser extends User {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "assignedInvitedUsers")
    @Builder.Default
    private List<Project> assignedProjects = new ArrayList<>();

    @Override
    public void addAssignedProject(Project project) {
        assignedProjects.add(project);
    }

    @Override
    public void removeAssignedProject(Project project) {
        assignedProjects.removeIf(p -> Objects.equals(p.getId(), project.getId()));
    }
}
