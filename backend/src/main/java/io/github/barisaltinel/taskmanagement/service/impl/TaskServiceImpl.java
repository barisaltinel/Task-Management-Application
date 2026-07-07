package io.github.barisaltinel.taskmanagement.service.impl;

import io.github.barisaltinel.taskmanagement.model.Task;
import io.github.barisaltinel.taskmanagement.model.TaskState;
import io.github.barisaltinel.taskmanagement.model.Project;
import io.github.barisaltinel.taskmanagement.model.User;
import io.github.barisaltinel.taskmanagement.repository.ProjectRepository;
import io.github.barisaltinel.taskmanagement.repository.TaskRepository;
import io.github.barisaltinel.taskmanagement.repository.UserRepository;
import io.github.barisaltinel.taskmanagement.service.TaskService;
import io.github.barisaltinel.taskmanagement.exception.AccessDeniedException;
import io.github.barisaltinel.taskmanagement.exception.ProjectNotFoundException;
import io.github.barisaltinel.taskmanagement.exception.TaskCannotBeModifiedException;
import io.github.barisaltinel.taskmanagement.exception.TaskNotFoundException;
import io.github.barisaltinel.taskmanagement.exception.UserNotFoundException;
import io.github.barisaltinel.taskmanagement.util.SecurityUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskServiceImpl(TaskRepository taskRepository, ProjectRepository projectRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Task> getAllTasks() {
        if (SecurityUtils.hasAnyRole("ADMIN", "PROJECT_MANAGER", "TEAM_LEADER")) {
            return taskRepository.findAllByDeletedFalseAndProjectDeletedFalseOrderByIdAsc();
        }

        String currentUsername = SecurityUtils.getCurrentUsername();
        if (!StringUtils.hasText(currentUsername)) {
            throw new AccessDeniedException();
        }

        return taskRepository.findAllByDeletedFalseAndProjectDeletedFalseAndAssigneeDeletedFalseAndAssigneeEmailIgnoreCaseOrderByIdAsc(currentUsername);
    }

    @Override
    public Task findById(@NonNull Long id) {
        Long requiredId = requireId(id, "Task id");
        if (SecurityUtils.hasAnyRole("ADMIN", "PROJECT_MANAGER", "TEAM_LEADER")) {
            return taskRepository.findByIdAndDeletedFalseAndProjectDeletedFalse(requiredId)
                    .orElseThrow(TaskNotFoundException::new);
        }

        String currentUsername = SecurityUtils.getCurrentUsername();
        if (!StringUtils.hasText(currentUsername)) {
            throw new AccessDeniedException();
        }

        return taskRepository.findByIdAndDeletedFalseAndProjectDeletedFalseAndAssigneeDeletedFalseAndAssigneeEmailIgnoreCase(requiredId, currentUsername)
                .orElseThrow(AccessDeniedException::new);
    }

    @Override
    public Task create(Task task, Long projectId, Long assigneeId) {
        if (task == null) {
            throw new IllegalArgumentException("Task details are required");
        }

        task.setProject(resolveProject(projectId));
        task.setAssignee(resolveUser(assigneeId));
        task.setState(TaskState.BACKLOG);
        task.setReason(null);
        task.setDeleted(false);
        return taskRepository.save(task);
    }

    @Override
    public Task update(@NonNull Long id, Task taskDetails, Long projectId, Long assigneeId) {
        if (taskDetails == null) {
            throw new IllegalArgumentException("Task details are required");
        }

        Task existingTask = findById(id);

        if (existingTask.getState() == TaskState.COMPLETED) {
            throw new TaskCannotBeModifiedException();
        }

        existingTask.setTitle(taskDetails.getTitle());
        existingTask.setDescription(taskDetails.getDescription());
        existingTask.setPriority(taskDetails.getPriority());
        existingTask.setState(taskDetails.getState());
        existingTask.setProject(resolveProject(projectId));
        existingTask.setAssignee(resolveUser(assigneeId));
        if (taskDetails.getState() != TaskState.CANCELLED) {
            existingTask.setReason(null);
        }

        return taskRepository.save(existingTask);
    }

    @Override
    public Task cancel(@NonNull Long id, String reason) {
        Task task = findById(id);

        if (task.getState() == TaskState.COMPLETED) {
            throw new TaskCannotBeModifiedException();
        }

        if (!StringUtils.hasText(reason)) {
            throw new IllegalArgumentException("A reason must be provided when cancelling a task");
        }

        task.setState(TaskState.CANCELLED);
        task.setReason(reason.trim());
        return taskRepository.save(task);
    }

    private Project resolveProject(Long projectId) {
        Long requiredProjectId = requireId(projectId, "Project id");
        return projectRepository.findByIdAndDeletedFalse(requiredProjectId)
                .orElseThrow(ProjectNotFoundException::new);
    }

    private User resolveUser(Long userId) {
        Long requiredUserId = requireId(userId, "Assignee id");
        return userRepository.findByIdAndDeletedFalse(requiredUserId)
                .orElseThrow(UserNotFoundException::new);
    }

    private @NonNull Long requireId(@Nullable Long id, String fieldName) {
        return Objects.requireNonNull(id, fieldName + " is required");
    }
}



