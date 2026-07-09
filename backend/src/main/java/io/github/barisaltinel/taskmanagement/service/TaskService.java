package io.github.barisaltinel.taskmanagement.service;

import io.github.barisaltinel.taskmanagement.exception.TaskNotFoundException;
import io.github.barisaltinel.taskmanagement.model.Task;
import java.util.List;
import org.springframework.lang.NonNull;

public interface TaskService {
    List<Task> getAllTasks();

    Task findById(@NonNull Long id) throws TaskNotFoundException;

    Task create(Task task, Long projectId, Long assigneeId);

    Task update(@NonNull Long id, Task task, Long projectId, Long assigneeId) throws TaskNotFoundException;

    Task cancel(@NonNull Long id, String reason) throws TaskNotFoundException;
}
