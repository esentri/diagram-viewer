package io.domainlifecycles.diagramviewer.repository;

import io.domainlifecycles.diagramviewer.model.AuthenticatedUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthenticatedUserRepository extends CrudRepository<AuthenticatedUser, UUID> {

    Optional<AuthenticatedUser> findByEmailAddress(final String emailAddress);

    Optional<AuthenticatedUser> findByApiKey(final UUID apiKey);

    AuthenticatedUser getByEmailAddress(final String emailAddress);
}
