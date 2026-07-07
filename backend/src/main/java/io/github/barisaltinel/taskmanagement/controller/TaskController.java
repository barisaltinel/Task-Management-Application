package io.github.barisaltinel.taskmanagement.controller;

import io.github.barisaltinel.taskmanagement.dto.ApiDtos;
import io.github.barisaltinel.taskmanagement.dto.ApiMapper;
import io.github.barisaltinel.taskmanagement.model.Task;
import io.github.barisaltinel.taskmanagement.service.TaskService;
import io.github.barisaltinel.taskmanagement.exception.TaskCannotBeModifiedException;
import io.github.barisaltinel.taskmanagement.exception.TaskNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TEAM_MEMBER', 'TEAM_LEADER', 'PROJECT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<ApiDtos.TaskResponse>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks().stream().map(ApiMapper::toTaskResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEAM_MEMBER', 'TEAM_LEADER', 'PROJECT_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiDtos.TaskResponse> getTaskById(@PathVariable @NonNull Long id) {
        return ResponseEntity.ok(ApiMapper.toTaskResponse(taskService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiDtos.TaskResponse> createTask(@Valid @RequestBody ApiDtos.TaskUpsertRequest request) {
        Task createdTask = taskService.create(ApiMapper.toTask(request), request.projectId(), request.assigneeId());
        return new ResponseEntity<>(ApiMapper.toTaskResponse(createdTask), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'TEAM_LEADER', 'ADMIN')")
    public ResponseEntity<ApiDtos.TaskResponse> updateTask(
            @PathVariable @NonNull Long id,
            @Valid @RequestBody ApiDtos.TaskUpsertRequest request
    ) {
        Task updatedTask = taskService.update(id, ApiMapper.toTask(request), request.projectId(), request.assigneeId());
        return ResponseEntity.ok(ApiMapper.toTaskResponse(updatedTask));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiDtos.TaskResponse> cancelTask(@PathVariable @NonNull Long id, @RequestParam String reason) {
        return ResponseEntity.ok(ApiMapper.toTaskResponse(taskService.cancel(id, reason)));
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<String> handleTaskNotFound(TaskNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(TaskCannotBeModifiedException.class)
    public ResponseEntity<String> handleTaskCannotBeModified(TaskCannotBeModifiedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}



