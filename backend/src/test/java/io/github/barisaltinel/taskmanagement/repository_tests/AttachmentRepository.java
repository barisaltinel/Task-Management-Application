package io.github.barisaltinel.taskmanagement.repository_tests;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.barisaltinel.taskmanagement.model.Attachment;
import io.github.barisaltinel.taskmanagement.model.Task;
import io.github.barisaltinel.taskmanagement.model.TaskPriority;
import io.github.barisaltinel.taskmanagement.model.TaskState;
import io.github.barisaltinel.taskmanagement.repository.AttachmentRepository;
import io.github.barisaltinel.taskmanagement.repository.TaskRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@DataJpaTest
class AttachmentRepositoryTest {

  @Autowired private AttachmentRepository attachmentRepository;

  @Autowired private TaskRepository taskRepository;

  private Task testTask;

  @BeforeEach
  void setUp() {
    testTask = new Task();
    testTask.setTitle("Test Task");
    testTask.setDescription("This is a test task");
    testTask.setState(TaskState.BACKLOG);
    testTask.setPriority(TaskPriority.MEDIUM);
    testTask = requireTask(taskRepository.save(testTask), "Saved task is required");
  }

  @Test
  void shouldSaveAndRetrieveAttachment() {
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
    Attachment savedAttachment = attachmentRepository.save(attachment);

    Optional<Attachment> foundAttachment =
        attachmentRepository.findById(
            requireId(savedAttachment.getId(), "Attachment id is required"));
    assertThat(foundAttachment).isPresent();
    assertThat(foundAttachment.get().getFileName()).isEqualTo("file.txt");
  }

  @Test
  void shouldSoftDeleteAttachment() {
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
    Attachment savedAttachment = attachmentRepository.save(attachment);

    savedAttachment.setDeleted(true);
    attachmentRepository.save(savedAttachment);

    Optional<Attachment> foundAttachment =
        attachmentRepository.findById(
            requireId(savedAttachment.getId(), "Attachment id is required"));
    assertThat(foundAttachment).isPresent();
    assertThat(foundAttachment.get().isDeleted()).isTrue();
  }

  @Test
  void shouldNotFindDeletedAttachment() {
    Attachment attachment =
        new Attachment(
            null,
            "file.txt",
            "uploads/file.txt",
            "text/plain",
            123L,
            testTask,
            LocalDateTime.now(),
            true);
    attachmentRepository.save(attachment);

    Optional<Attachment> foundAttachment =
        attachmentRepository.findById(requireId(attachment.getId(), "Attachment id is required"));
    assertThat(foundAttachment).isPresent();
    assertThat(foundAttachment.get().isDeleted()).isTrue();
  }

  @Test
  void shouldFindAllAttachmentsByTaskId() {
    Attachment attachment1 =
        new Attachment(
            null,
            "file1.txt",
            "uploads/file1.txt",
            "text/plain",
            123L,
            testTask,
            LocalDateTime.now(),
            false);
    Attachment attachment2 =
        new Attachment(
            null,
            "file2.txt",
            "uploads/file2.txt",
            "text/plain",
            456L,
            testTask,
            LocalDateTime.now(),
            false);
    attachmentRepository.saveAll(requireAttachments(List.of(attachment1, attachment2)));

    List<Attachment> attachments = attachmentRepository.findAll();
    assertThat(attachments).hasSize(2);
  }

  @Test
  void shouldNotFindDeletedAttachmentsInList() {
    Attachment attachment1 =
        new Attachment(
            null,
            "file1.txt",
            "uploads/file1.txt",
            "text/plain",
            123L,
            testTask,
            LocalDateTime.now(),
            false);
    Attachment attachment2 =
        new Attachment(
            null,
            "file2.txt",
            "uploads/file2.txt",
            "text/plain",
            456L,
            testTask,
            LocalDateTime.now(),
            true);
    attachmentRepository.saveAll(requireAttachments(List.of(attachment1, attachment2)));

    List<Attachment> attachments =
        attachmentRepository.findAll().stream().filter(a -> !a.isDeleted()).toList();

    assertThat(attachments).hasSize(1);
    assertThat(attachments.get(0).getFileName()).isEqualTo("file1.txt");
  }

  @Test
  void shouldReturnEmptyListIfNoAttachmentsExist() {
    List<Attachment> attachments = attachmentRepository.findAll();
    assertThat(attachments).isEmpty();
  }

  private @NonNull Task requireTask(@Nullable Task task, String message) {
    return Objects.requireNonNull(task, message);
  }

  private @NonNull Long requireId(@Nullable Long id, String message) {
    return Objects.requireNonNull(id, message);
  }

  private @NonNull Iterable<Attachment> requireAttachments(@Nullable List<Attachment> attachments) {
    return new ArrayList<>(Objects.requireNonNull(attachments, "Attachments are required"));
  }
}
