package io.domainlifecycles.diagramviewer.repository;

import io.domainlifecycles.diagramviewer.model.RegisteredUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegisteredUserRepository extends CrudRepository<RegisteredUser, UUID> {

    Optional<RegisteredUser> findByEmailAddress(final String emailAddress);

    Optional<RegisteredUser> findByApiKey(final UUID apiKey);

    RegisteredUser getByEmailAddress(final String emailAddress);
}
