package io.domainlifecycles.diagramviewer.repository;

import io.domainlifecycles.diagramviewer.model.TemporaryUser;
import io.domainlifecycles.diagramviewer.model.User;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemporaryUserRepository extends CrudRepository<TemporaryUser, Long> {

    Optional<TemporaryUser> findByEmailAddress(final String emailAddress);
}
