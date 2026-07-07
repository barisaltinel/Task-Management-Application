package io.github.barisaltinel.taskmanagement.service;

import io.github.barisaltinel.taskmanagement.model.Task;
import io.github.barisaltinel.taskmanagement.exception.TaskNotFoundException;
import org.springframework.lang.NonNull;

import java.util.List;

public interface TaskService {
    List<Task> getAllTasks();
    Task findById(@NonNull Long id) throws TaskNotFoundException;
    Task create(Task task, Long projectId, Long assigneeId);
    Task update(@NonNull Long id, Task task, Long projectId, Long assigneeId) throws TaskNotFoundException;
    Task cancel(@NonNull Long id, String reason) throws TaskNotFoundException;
}



