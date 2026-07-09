package io.github.barisaltinel.taskmanagement.integration_tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.barisaltinel.taskmanagement.model.Attachment;
import io.github.barisaltinel.taskmanagement.model.Project;
import io.github.barisaltinel.taskmanagement.model.ProjectStatus;
import io.github.barisaltinel.taskmanagement.model.Task;
import io.github.barisaltinel.taskmanagement.model.TaskPriority;
import io.github.barisaltinel.taskmanagement.model.TaskState;
import io.github.barisaltinel.taskmanagement.model.User;
import io.github.barisaltinel.taskmanagement.repository.AttachmentRepository;
import io.github.barisaltinel.taskmanagement.repository.ProjectRepository;
import io.github.barisaltinel.taskmanagement.repository.TaskRepository;
import io.github.barisaltinel.taskmanagement.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AttachmentIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private AttachmentRepository attachmentRepository;

  @Autowired private TaskRepository taskRepository;

  @Autowired private ProjectRepository projectRepository;

  @Autowired private UserRepository userRepository;

  private Task testTask;
  private Project testProject;

  @BeforeEach
  void setUp() {
    User assignee = new User();
    assignee.setName("Team Member");
    assignee.setEmail("team_member@example.com");
    assignee.setPassword("encoded-password");
    assignee.setRole("TEAM_MEMBER");
    assignee.setDeleted(false);
    assignee = userRepository.save(assignee);

    testProject = new Project();
    testProject.setTitle("Attachment Project");
    testProject.setDescription("Project for attachment integration tests");
    testProject.setStatus(ProjectStatus.IN_PROGRESS);
    testProject.setDepartmentName("IT");
    testProject = projectRepository.save(testProject);

    testTask = new Task();
    testTask.setTitle("Test Task");
    testTask.setDescription("This is a test task");
    testTask.setState(TaskState.BACKLOG);
    testTask.setPriority(TaskPriority.MEDIUM);
    testTask.setProject(testProject);
    testTask.setAssignee(assignee);
    testTask = requireTask(taskRepository.save(testTask), "Saved task is required");
  }

  @Test
  @WithMockUser(username = "team_member@example.com", roles = "TEAM_MEMBER")
  void shouldUploadAttachmentSuccessfully() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "test-file.txt", "text/plain", "Hello, World!".getBytes());

    mockMvc
        .perform(
            multipart("/api/attachments")
                .file(file)
                .param("taskId", testTask.getId().toString())
                .contentType(requireMediaType(MediaType.MULTIPART_FORM_DATA)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.fileName").value("test-file.txt"));

    assertThat(attachmentRepository.findAll()).hasSize(1);
  }

  @Test
  @WithMockUser(username = "team_member@example.com", roles = "TEAM_MEMBER")
  void shouldFailToUploadEmptyFile() throws Exception {
    MockMultipartFile emptyFile =
        new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

    mockMvc
        .perform(
            multipart("/api/attachments")
                .file(emptyFile)
                .param("taskId", testTask.getId().toString())
                .contentType(requireMediaType(MediaType.MULTIPART_FORM_DATA)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldFailToUploadAttachmentWithoutAuthentication() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "unauthorized.txt", "text/plain", "Unauthorized user".getBytes());

    mockMvc
        .perform(
            multipart("/api/attachments")
                .file(file)
                .param("taskId", testTask.getId().toString())
                .contentType(requireMediaType(MediaType.MULTIPART_FORM_DATA)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(username = "team_member@example.com", roles = "TEAM_MEMBER")
  void shouldFailToUploadFileToInvalidTask() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "invalid-task-file.txt", "text/plain", "Invalid Task".getBytes());

    mockMvc
        .perform(
            multipart("/api/attachments")
                .file(file)
                .param("taskId", "99999")
                .contentType(requireMediaType(MediaType.MULTIPART_FORM_DATA)))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = "team_member@example.com", roles = "TEAM_MEMBER")
  void shouldGetAttachmentById() throws Exception {
    Attachment attachment =
        new Attachment(
            null,
            "file.txt",
            "uploads/file.txt",
            "text/plain",
            123L,
            testTask,
            LocalDateTime.now(),
            false);
    attachment = attachmentRepository.save(attachment);

    mockMvc
        .perform(
            get("/api/attachments/" + attachment.getId())
                .contentType(requireMediaType(MediaType.APPLICATION_JSON)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fileName").value("file.txt"));
  }

  @Test
  @WithMockUser(username = "team_member@example.com", roles = "TEAM_MEMBER")
  void shouldSoftDeleteAttachment() throws Exception {
    Attachment attachment =
        new Attachment(
            null,
            "file.txt",
            "uploads/file.txt",
            "text/plain",
            123L,
            testTask,
            LocalDateTime.now(),
            false);
    attachment = attachmentRepository.save(attachment);

    mockMvc
        .perform(delete("/api/attachments/" + attachment.getId()))
        .andExpect(status().isNoContent());

    Attachment deletedAttachment =
        attachmentRepository
            .findById(requireId(attachment.getId(), "Attachment id is required"))
            .orElseThrow();
    assertThat(deletedAttachment.isDeleted()).isTrue();
  }

  @Test
  @WithMockUser(username = "team_member@example.com", roles = "TEAM_MEMBER")
  void shouldReturnNotFoundForDeletedAttachment() throws Exception {
    Attachment attachment =
        new Attachment(
            null,
            "deleted-file.txt",
            "uploads/deleted-file.txt",
            "text/plain",
            123L,
            testTask,
            LocalDateTime.now(),
            true);
    attachmentRepository.save(attachment);

    mockMvc.perform(get("/api/attachments/" + attachment.getId())).andExpect(status().isNotFound());
  }

  private @NonNull Task requireTask(@Nullable Task task, String message) {
    return Objects.requireNonNull(task, message);
  }

  private @NonNull MediaType requireMediaType(@Nullable MediaType mediaType) {
    return Objects.requireNonNull(mediaType, "MediaType is required");
  }

  private @NonNull Long requireId(@Nullable Long id, String message) {
    return Objects.requireNonNull(id, message);
  }
}
