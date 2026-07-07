package io.github.barisaltinel.taskmanagement.service_tests;

import io.github.barisaltinel.taskmanagement.model.Attachment;
import io.github.barisaltinel.taskmanagement.model.Project;
import io.github.barisaltinel.taskmanagement.model.ProjectStatus;
import io.github.barisaltinel.taskmanagement.model.Task;
import io.github.barisaltinel.taskmanagement.model.User;
import io.github.barisaltinel.taskmanagement.repository.AttachmentRepository;
import io.github.barisaltinel.taskmanagement.repository.TaskRepository;
import io.github.barisaltinel.taskmanagement.exception.AttachmentNotFoundException;
import io.github.barisaltinel.taskmanagement.service.impl.AttachmentServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private AttachmentServiceImpl attachmentService;

    private Attachment mockAttachment;
    private Task mockTask;
    private Project mockProject;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockProject = new Project();
        mockProject.setId(10L);
        mockProject.setTitle("Project");
        mockProject.setDescription("Description");
        mockProject.setDepartmentName("IT");
        mockProject.setStatus(ProjectStatus.IN_PROGRESS);
        mockProject.setDeleted(false);

        mockUser = new User();
        mockUser.setId(20L);
        mockUser.setName("Assignee");
        mockUser.setEmail("assignee@example.com");
        mockUser.setRole("TEAM_MEMBER");
        mockUser.setDeleted(false);

        mockTask = new Task();
        mockTask.setId(1L);
        mockTask.setProject(mockProject);
        mockTask.setAssignee(mockUser);
        mockTask.setDeleted(false);

        mockAttachment = new Attachment();
        mockAttachment.setId(1L);
        mockAttachment.setFileName("test.pdf");
        mockAttachment.setFilePath("uploads/test.pdf");
        mockAttachment.setMimeType("application/pdf");
        mockAttachment.setFileSize(1024L);
        mockAttachment.setTask(mockTask);
        mockAttachment.setDeleted(false);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin@example.com",
                        "N/A",
                        Set.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnAllAttachments() {
        when(attachmentRepository.findAllByDeletedFalseAndTaskDeletedFalseAndTaskProjectDeletedFalseOrderByIdAsc())
                .thenReturn(List.of(mockAttachment));
        List<Attachment> attachments = attachmentService.getAllAttachments();
        assertThat(attachments).hasSize(1);
    }

    @Test
    void shouldFindAttachmentById() {
        when(attachmentRepository.findByIdAndDeletedFalseAndTaskDeletedFalseAndTaskProjectDeletedFalse(1L))
                .thenReturn(Optional.of(mockAttachment));
        Attachment attachment = attachmentService.findById(1L);
        assertThat(attachment).isEqualTo(mockAttachment);
    }

    @Test
    void shouldThrowAttachmentNotFoundException() {
        when(attachmentRepository.findByIdAndDeletedFalseAndTaskDeletedFalseAndTaskProjectDeletedFalse(99L))
                .thenReturn(Optional.empty());
        Assertions.assertThrows(AttachmentNotFoundException.class, () -> attachmentService.findById(99L));
    }

    @Test
    void shouldUploadAttachment() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[1024]);
        when(taskRepository.findByIdAndDeletedFalseAndProjectDeletedFalse(1L)).thenReturn(Optional.of(mockTask));
        when(attachmentRepository.save(anyAttachment())).thenReturn(requireAttachment(mockAttachment, "Mock attachment is required"));

        Attachment uploadedAttachment = attachmentService.upload(file, 1L);

        assertThat(uploadedAttachment.getFileName()).isEqualTo("test.pdf");
        verify(attachmentRepository, times(1)).save(anyAttachment());
    }

    @Test
    void shouldSoftDeleteAttachment() {
        when(attachmentRepository.findByIdAndDeletedFalseAndTaskDeletedFalseAndTaskProjectDeletedFalse(1L))
                .thenReturn(Optional.of(mockAttachment));
        attachmentService.softDelete(1L);
        assertThat(mockAttachment.isDeleted()).isTrue();
        verify(attachmentRepository, times(1)).save(requireAttachment(mockAttachment, "Mock attachment is required"));
    }

    @Test
    void shouldUpdateAttachment() {
        Attachment updatedAttachment = new Attachment();
        updatedAttachment.setFileName("updated.pdf");

        when(attachmentRepository.findByIdAndDeletedFalseAndTaskDeletedFalseAndTaskProjectDeletedFalse(1L))
                .thenReturn(Optional.of(mockAttachment));
        when(attachmentRepository.save(anyAttachment())).thenReturn(requireAttachment(mockAttachment, "Mock attachment is required"));

        Attachment result = attachmentService.update(1L, updatedAttachment);

        assertThat(result.getFileName()).isEqualTo("updated.pdf");
        verify(attachmentRepository, times(1)).save(requireAttachment(mockAttachment, "Mock attachment is required"));
    }

    private @NonNull Attachment requireAttachment(@Nullable Attachment attachment, String message) {
        return Objects.requireNonNull(attachment, message);
    }

    private @NonNull Attachment anyAttachment() {
        return any(Attachment.class);
    }
}



