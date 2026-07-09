package io.github.barisaltinel.taskmanagement.controller_tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import io.github.barisaltinel.taskmanagement.controller.TaskController;
import io.github.barisaltinel.taskmanagement.dto.ApiDtos;
import io.github.barisaltinel.taskmanagement.exception.TaskNotFoundException;
import io.github.barisaltinel.taskmanagement.model.Task;
import io.github.barisaltinel.taskmanagement.model.TaskPriority;
import io.github.barisaltinel.taskmanagement.model.TaskState;
import io.github.barisaltinel.taskmanagement.service.TaskService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

  @Mock private TaskService taskService;

  @InjectMocks private TaskController taskController;

  private Task mockTask;

  @BeforeEach
  void setUp() {
    mockTask = new Task();
    mockTask.setId(1L);
    mockTask.setTitle("Test Task");
    mockTask.setDescription("This is a test task");
    mockTask.setState(TaskState.BACKLOG);
  }

  @Test
  @WithMockUser
  void shouldReturnAllTasks() {
    when(taskService.getAllTasks()).thenReturn(List.of(mockTask));
    ResponseEntity<List<ApiDtos.TaskResponse>> response = taskController.getAllTasks();
    assertThat(response.getBody()).isNotNull().hasSize(1);
    assertThat(response.getBody().get(0).title()).isEqualTo(mockTask.getTitle());
  }

  @Test
  @WithMockUser
  void shouldReturnTaskById() {
    when(taskService.findById(1L)).thenReturn(mockTask);
    ResponseEntity<ApiDtos.TaskResponse> response = taskController.getTaskById(1L);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().title()).isEqualTo(mockTask.getTitle());
  }

  @Test
  @WithMockUser
  void shouldThrowExceptionWhenTaskNotFound() {
    when(taskService.findById(99L)).thenThrow(new TaskNotFoundException());
    assertThatThrownBy(() -> taskController.getTaskById(99L))
        .isInstanceOf(TaskNotFoundException.class)
        .hasMessageContaining("Task not found");
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void shouldCreateTask() {
    ApiDtos.TaskUpsertRequest request =
        new ApiDtos.TaskUpsertRequest(
            "Test Task", "This is a test task", TaskPriority.MEDIUM, TaskState.BACKLOG, 10L, 20L);
    when(taskService.create(any(Task.class), anyLong(), anyLong())).thenReturn(mockTask);
    ResponseEntity<ApiDtos.TaskResponse> response = taskController.createTask(request);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().title()).isEqualTo(mockTask.getTitle());
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void shouldUpdateTask() {
    ApiDtos.TaskUpsertRequest request =
        new ApiDtos.TaskUpsertRequest(
            "Test Task",
            "This is a test task",
            TaskPriority.MEDIUM,
            TaskState.IN_PROGRESS,
            10L,
            20L);
    when(taskService.update(anyLong(), any(Task.class), anyLong(), anyLong())).thenReturn(mockTask);
    ResponseEntity<ApiDtos.TaskResponse> response = taskController.updateTask(1L, request);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().title()).isEqualTo(mockTask.getTitle());
  }

  @Test
  @WithMockUser(username = "project_manager", roles = "PROJECT_MANAGER")
  void shouldCancelTask() {
    when(taskService.cancel(anyLong(), anyString())).thenReturn(mockTask);
    ResponseEntity<ApiDtos.TaskResponse> response =
        taskController.cancelTask(1L, "Task no longer needed");
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().title()).isEqualTo(mockTask.getTitle());
  }
}
