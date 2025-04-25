package io.domainlifecycles.diagramviewer.repository;

import io.domainlifecycles.diagramviewer.model.InvitedUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvitedUserRepository extends CrudRepository<InvitedUser, UUID> {

    Optional<InvitedUser> findByEmailAddress(final String emailAddress);
}
