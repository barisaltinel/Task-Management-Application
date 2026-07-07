package io.github.barisaltinel.taskmanagement.repository;

import io.github.barisaltinel.taskmanagement.model.Attachment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    @EntityGraph(attributePaths = "task")
    List<Attachment> findAllByDeletedFalseAndTaskDeletedFalseAndTaskProjectDeletedFalseOrderByIdAsc();

    @EntityGraph(attributePaths = "task")
    List<Attachment> findAllByDeletedFalseAndTaskDeletedFalseAndTaskProjectDeletedFalseAndTaskAssigneeDeletedFalseAndTaskAssigneeEmailIgnoreCaseOrderByIdAsc(String email);

    @EntityGraph(attributePaths = "task")
    Optional<Attachment> findByIdAndDeletedFalseAndTaskDeletedFalseAndTaskProjectDeletedFalse(Long id);

    @EntityGraph(attributePaths = "task")
    Optional<Attachment> findByIdAndDeletedFalseAndTaskDeletedFalseAndTaskProjectDeletedFalseAndTaskAssigneeDeletedFalseAndTaskAssigneeEmailIgnoreCase(Long id, String email);
}


