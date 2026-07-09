package io.github.barisaltinel.taskmanagement.repository;

import io.github.barisaltinel.taskmanagement.model.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    @EntityGraph(attributePaths = "teamMembers")
    List<Project> findAllByDeletedFalseOrderByIdAsc();

    @EntityGraph(attributePaths = "teamMembers")
    Optional<Project> findByIdAndDeletedFalse(Long id);
}
