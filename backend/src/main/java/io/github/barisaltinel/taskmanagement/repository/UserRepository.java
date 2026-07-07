package io.github.barisaltinel.taskmanagement.repository;

import io.github.barisaltinel.taskmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByDeletedFalseOrderByIdAsc();

    Optional<User> findByIdAndDeletedFalse(Long id);

    Optional<User> findByEmailIgnoreCaseAndDeletedFalse(String email);

    boolean existsByEmailIgnoreCaseAndDeletedFalse(String email);

    List<User> findAllByIdInAndDeletedFalse(List<Long> ids);
}


