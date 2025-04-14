package io.domainlifecycles.diagramviewer.repository;

import io.domainlifecycles.diagramviewer.model.AuthenticatedUser;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthenticatedUserRepository extends CrudRepository<AuthenticatedUser, Long> {

    Optional<AuthenticatedUser> findByEmailAddress(final String emailAddress);
    AuthenticatedUser getByEmailAddress(final String emailAddress);
}
