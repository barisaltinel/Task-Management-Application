package io.github.barisaltinel.taskmanagement.service.impl;

import io.github.barisaltinel.taskmanagement.model.Comment;
import io.github.barisaltinel.taskmanagement.model.Task;
import io.github.barisaltinel.taskmanagement.model.User;
import io.github.barisaltinel.taskmanagement.repository.CommentRepository;
import io.github.barisaltinel.taskmanagement.repository.TaskRepository;
import io.github.barisaltinel.taskmanagement.repository.UserRepository;
import io.github.barisaltinel.taskmanagement.service.CommentService;
import io.github.barisaltinel.taskmanagement.exception.AccessDeniedException;
import io.github.barisaltinel.taskmanagement.exception.CommentNotFoundException;
import io.github.barisaltinel.taskmanagement.exception.TaskNotFoundException;
import io.github.barisaltinel.taskmanagement.exception.UserNotFoundException;
import io.github.barisaltinel.taskmanagement.util.SecurityUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public CommentServiceImpl(CommentRepository commentRepository, TaskRepository taskRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Comment> getAllComments() {
        if (SecurityUtils.hasAnyRole("ADMIN", "PROJECT_MANAGER", "TEAM_LEADER")) {
            return commentRepository.findAllByTaskDeletedFalseAndTaskProjectDeletedFalseOrderByIdAsc();
        }

        String currentUsername = SecurityUtils.getCurrentUsername();
        if (!StringUtils.hasText(currentUsername)) {
            throw new AccessDeniedException();
        }

        return commentRepository
                .findAllByTaskDeletedFalseAndTaskProjectDeletedFalseAndTaskAssigneeDeletedFalseAndTaskAssigneeEmailIgnoreCaseOrderByIdAsc(currentUsername);
    }

    @Override
    public Comment findById(Long id) {
        Long requiredId = requireId(id, "Comment id");

        if (SecurityUtils.hasAnyRole("ADMIN", "PROJECT_MANAGER", "TEAM_LEADER")) {
            return commentRepository.findByIdAndTaskDeletedFalseAndTaskProjectDeletedFalse(requiredId)
                    .orElseThrow(CommentNotFoundException::new);
        }

        String currentUsername = SecurityUtils.getCurrentUsername();
        if (!StringUtils.hasText(currentUsername)) {
            throw new AccessDeniedException();
        }

        return commentRepository
                .findByIdAndTaskDeletedFalseAndTaskProjectDeletedFalseAndTaskAssigneeDeletedFalseAndTaskAssigneeEmailIgnoreCase(requiredId, currentUsername)
                .orElseThrow(AccessDeniedException::new);
    }

    @Override
    public Comment create(String text, Long taskId) {
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException("Comment text cannot be empty");
        }
        Long requiredTaskId = requireId(taskId, "Task id");

        String currentUsername = SecurityUtils.getCurrentUsername();
        if (!StringUtils.hasText(currentUsername)) {
            throw new AccessDeniedException();
        }

        User author = userRepository.findByEmailIgnoreCaseAndDeletedFalse(currentUsername)
                .orElseThrow(UserNotFoundException::new);
        Task task = taskRepository.findByIdAndDeletedFalseAndProjectDeletedFalse(requiredTaskId)
                .orElseThrow(TaskNotFoundException::new);

        if (!canAccessTask(task)) {
            throw new AccessDeniedException();
        }

        Comment newComment = new Comment();
        newComment.setText(text.trim());
        newComment.setAuthor(author);
        newComment.setTask(task);
        newComment.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(newComment);
    }

    private boolean canAccessTask(Task task) {
        if (SecurityUtils.hasAnyRole("ADMIN", "PROJECT_MANAGER", "TEAM_LEADER")) {
            return true;
        }
        String currentUsername = SecurityUtils.getCurrentUsername();
        if (!StringUtils.hasText(currentUsername) || task == null || task.getAssignee() == null) {
            return false;
        }

        String assigneeEmail = task.getAssignee().getEmail();
        if (!StringUtils.hasText(assigneeEmail)) {
            return false;
        }

        return currentUsername.equalsIgnoreCase(assigneeEmail);
    }

    private @NonNull Long requireId(@Nullable Long id, String fieldName) {
        return Objects.requireNonNull(id, fieldName + " is required");
    }
}



