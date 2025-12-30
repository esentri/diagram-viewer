package io.domainlifecycles.diagramviewer.repository;

import io.domainlifecycles.diagramviewer.model.viewer.AppUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends CrudRepository<AppUser, UUID> {

    Optional<AppUser> findByEmailAddress(final String emailAddress);

    Optional<AppUser> findByApiKey(final UUID apiKey);

    AppUser getByEmailAddress(final String emailAddress);
}
