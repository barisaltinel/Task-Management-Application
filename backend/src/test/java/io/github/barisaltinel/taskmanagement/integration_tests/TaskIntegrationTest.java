package io.github.barisaltinel.taskmanagement.integration_tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.barisaltinel.taskmanagement.dto.ApiDtos;
import io.github.barisaltinel.taskmanagement.model.Project;
import io.github.barisaltinel.taskmanagement.model.ProjectStatus;
import io.github.barisaltinel.taskmanagement.model.Task;
import io.github.barisaltinel.taskmanagement.model.TaskPriority;
import io.github.barisaltinel.taskmanagement.model.TaskState;
import io.github.barisaltinel.taskmanagement.repository.ProjectRepository;
import io.github.barisaltinel.taskmanagement.repository.TaskRepository;
import io.github.barisaltinel.taskmanagement.repository.UserRepository;
import io.github.barisaltinel.taskmanagement.model.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private Task task;
    private Project project;
    private User assignee;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setTitle("Test Project");
        project.setDescription("Project for testing");
        project.setStatus(ProjectStatus.IN_PROGRESS);
        project.setDepartmentName("IT");
        project = projectRepository.save(project);

        assignee = new User();
        assignee.setName("Task Assignee");
        assignee.setEmail("task.assignee@example.com");
        assignee.setPassword("encoded-password");
        assignee.setRole("TEAM_MEMBER");
        assignee.setDeleted(false);
        assignee = userRepository.save(assignee);

        task = new Task();
        task.setTitle("Test Task");
        task.setDescription("This is a test task");
        task.setState(TaskState.BACKLOG);
        task.setPriority(TaskPriority.MEDIUM);
        task.setStartDate(LocalDate.now());
        task.setDueDate(LocalDate.now().plusDays(5));
        task.setProject(project);
        task.setAssignee(assignee);
        task = requireTask(taskRepository.save(task), "Saved task is required");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldCreateTaskSuccessfully() throws Exception {
        ApiDtos.TaskUpsertRequest newTask = new ApiDtos.TaskUpsertRequest(
                "New Task",
                "Integration Test",
                TaskPriority.HIGH,
                TaskState.IN_PROGRESS,
                LocalDate.now(),
                LocalDate.now().plusDays(9),
                project.getId(),
                assignee.getId()
        );

        mockMvc.perform(post("/api/tasks")
                        .contentType(requireMediaType(MediaType.APPLICATION_JSON))
                        .content(requireContent(objectMapper.writeValueAsString(newTask))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Task"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldCancelTaskSuccessfully() throws Exception {
        mockMvc.perform(put("/api/tasks/" + task.getId() + "/cancel")
                        .param("reason", "No longer needed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("CANCELLED"))
                .andExpect(jsonPath("$.reason").value("No longer needed"));
    }

    private @NonNull Task requireTask(@Nullable Task task, String message) {
        return Objects.requireNonNull(task, message);
    }

    private @NonNull MediaType requireMediaType(@Nullable MediaType mediaType) {
        return Objects.requireNonNull(mediaType, "MediaType is required");
    }

    private @NonNull String requireContent(@Nullable String content) {
        return Objects.requireNonNull(content, "Serialized content is required");
    }
}


