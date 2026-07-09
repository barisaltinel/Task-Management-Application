package io.github.barisaltinel.taskmanagement.repository;

import io.github.barisaltinel.taskmanagement.model.Comment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @EntityGraph(attributePaths = {"author", "task"})
    List<Comment> findAllByTaskDeletedFalseAndTaskProjectDeletedFalseOrderByIdAsc();

    @EntityGraph(attributePaths = {"author", "task"})
    List<Comment>
            findAllByTaskDeletedFalseAndTaskProjectDeletedFalseAndTaskAssigneeDeletedFalseAndTaskAssigneeEmailIgnoreCaseOrderByIdAsc(
                    String email);

    @EntityGraph(attributePaths = {"author", "task"})
    Optional<Comment> findByIdAndTaskDeletedFalseAndTaskProjectDeletedFalse(Long id);

    @EntityGraph(attributePaths = {"author", "task"})
    Optional<Comment>
            findByIdAndTaskDeletedFalseAndTaskProjectDeletedFalseAndTaskAssigneeDeletedFalseAndTaskAssigneeEmailIgnoreCase(
                    Long id, String email);
}
