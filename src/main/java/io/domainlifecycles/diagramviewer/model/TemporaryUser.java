package io.domainlifecycles.diagramviewer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * An Instance of a Temporary User is created, when a Project admin adds a new email-address to the Project members and
 * the given email-address is not known to the system. The temporary user remains in the database for as long as he signs up
 * for the first time, then this instance will be deleted and instead a new {@link AuthenticatedUser} created.
 * Temporary Users allow the project admin to give users access to projects, although they haven't signed up yet.
 *
 * @author leonvoellinger
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "TEMPORARY_USER")
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class TemporaryUser extends User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long temporaryUserId;
}
