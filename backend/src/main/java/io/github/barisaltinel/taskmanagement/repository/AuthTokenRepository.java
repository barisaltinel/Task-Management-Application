package io.github.barisaltinel.taskmanagement.repository;

import io.github.barisaltinel.taskmanagement.model.AuthToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<AuthToken> findByTokenHashAndRevokedFalse(String tokenHash);
}
