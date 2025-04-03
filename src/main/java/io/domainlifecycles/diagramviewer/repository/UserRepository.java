package io.domainlifecycles.diagramviewer.repository;

import io.domainlifecycles.diagramviewer.model.User;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByEmailAddress(final String emailAddress);
    User getByEmailAddress(final String emailAddress);
}
