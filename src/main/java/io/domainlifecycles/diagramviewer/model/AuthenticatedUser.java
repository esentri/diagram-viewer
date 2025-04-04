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

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "AUTHENTICATED_USER")
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class AuthenticatedUser extends User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long authenticatedUserId;
}
