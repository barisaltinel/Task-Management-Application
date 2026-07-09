package io.github.barisaltinel.taskmanagement.repository;

import io.github.barisaltinel.taskmanagement.model.Task;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
  @EntityGraph(attributePaths = {"project", "assignee"})
  List<Task> findAllByDeletedFalseAndProjectDeletedFalseOrderByIdAsc();

  @EntityGraph(attributePaths = {"project", "assignee"})
  List<Task>
      findAllByDeletedFalseAndProjectDeletedFalseAndAssigneeDeletedFalseAndAssigneeEmailIgnoreCaseOrderByIdAsc(
          String email);

  @EntityGraph(attributePaths = {"project", "assignee"})
  Optional<Task> findByIdAndDeletedFalseAndProjectDeletedFalse(Long id);

  @EntityGraph(attributePaths = {"project", "assignee"})
  Optional<Task>
      findByIdAndDeletedFalseAndProjectDeletedFalseAndAssigneeDeletedFalseAndAssigneeEmailIgnoreCase(
          Long id, String email);
}
