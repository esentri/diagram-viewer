package io.domainlifecycles.diagramviewer.repository;

import io.domainlifecycles.diagramviewer.model.TemporaryUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemporaryUserRepository extends CrudRepository<TemporaryUser, UUID> {

    Optional<TemporaryUser> findByEmailAddress(final String emailAddress);
}
