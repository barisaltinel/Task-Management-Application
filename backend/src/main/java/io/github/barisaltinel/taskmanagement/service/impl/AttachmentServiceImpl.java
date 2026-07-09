package io.github.barisaltinel.taskmanagement.service.impl;

import io.github.barisaltinel.taskmanagement.cache.TaskManagementCacheCoordinator;
import io.github.barisaltinel.taskmanagement.cache.TaskManagementCacheNames;
import io.github.barisaltinel.taskmanagement.exception.AccessDeniedException;
import io.github.barisaltinel.taskmanagement.exception.AttachmentNotFoundException;
import io.github.barisaltinel.taskmanagement.exception.EmptyFileException;
import io.github.barisaltinel.taskmanagement.exception.TaskNotFoundException;
import io.github.barisaltinel.taskmanagement.messaging.TaskManagementEntityType;
import io.github.barisaltinel.taskmanagement.messaging.TaskManagementEventAction;
import io.github.barisaltinel.taskmanagement.messaging.TaskManagementEventPublisher;
import io.github.barisaltinel.taskmanagement.messaging.TaskManagementEvents;
import io.github.barisaltinel.taskmanagement.model.Attachment;
import io.github.barisaltinel.taskmanagement.model.Task;
import io.github.barisaltinel.taskmanagement.repository.AttachmentRepository;
import io.github.barisaltinel.taskmanagement.repository.TaskRepository;
import io.github.barisaltinel.taskmanagement.service.AttachmentService;
import io.github.barisaltinel.taskmanagement.util.SecurityUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AttachmentServiceImpl implements AttachmentService {
    private static final String UPLOAD_DIR = "uploads/";
    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_MIME_TYPES =
            Set.of("application/pdf", "text/plain", "image/png", "image/jpeg");

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private TaskManagementEventPublisher eventPublisher = TaskManagementEventPublisher.noOp();
    private TaskManagementCacheCoordinator cacheCoordinator = TaskManagementCacheCoordinator.noOp();

    public AttachmentServiceImpl(AttachmentRepository attachmentRepository, TaskRepository taskRepository) {
        this.attachmentRepository = attachmentRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    @Cacheable(
            cacheNames = TaskManagementCacheNames.ATTACHMENT_LIST,
            key = "T(io.github.barisaltinel.taskmanagement.cache.TaskManagementCacheKeys).currentAccessScope()")
    public List<Attachment> getAllAttachments() {
        if (SecurityUtils.hasAnyRole("ADMIN", "PROJECT_MANAGER", "TEAM_LEADER")) {
            return attachmentRepository
                    .findAllByDeletedFalseAndTaskDeletedFalseAndTaskProjectDeletedFalseOrderByIdAsc();
        }

        String currentUsername = SecurityUtils.getCurrentUsername();
        if (!StringUtils.hasText(currentUsername)) {
            throw new AccessDeniedException();
        }

        return attachmentRepository
                .findAllByDeletedFalseAndTaskDeletedFalseAndTaskProjectDeletedFalseAndTaskAssigneeDeletedFalseAndTaskAssigneeEmailIgnoreCaseOrderByIdAsc(
                        currentUsername);
    }

    @Override
    @Cacheable(
            cacheNames = TaskManagementCacheNames.ATTACHMENT_DETAILS,
            key = "T(io.github.barisaltinel.taskmanagement.cache.TaskManagementCacheKeys).scopedId(#id)")
    public Attachment findById(Long id) {
        Long requiredId = requireId(id, "Attachment id");

        if (SecurityUtils.hasAnyRole("ADMIN", "PROJECT_MANAGER", "TEAM_LEADER")) {
            return attachmentRepository
                    .findByIdAndDeletedFalseAndTaskDeletedFalseAndTaskProjectDeletedFalse(requiredId)
                    .orElseThrow(AttachmentNotFoundException::new);
        }

        String currentUsername = SecurityUtils.getCurrentUsername();
        if (!StringUtils.hasText(currentUsername)) {
            throw new AccessDeniedException();
        }

        return attachmentRepository
                .findByIdAndDeletedFalseAndTaskDeletedFalseAndTaskProjectDeletedFalseAndTaskAssigneeDeletedFalseAndTaskAssigneeEmailIgnoreCase(
                        requiredId, currentUsername)
                .orElseThrow(AttachmentNotFoundException::new);
    }

    @Override
    @PreAuthorize("hasAnyRole('TEAM_MEMBER', 'TEAM_LEADER', 'PROJECT_MANAGER', 'ADMIN')")
    public Attachment upload(MultipartFile file, Long taskId) {
        if (file == null) {
            throw new IllegalArgumentException("File is required");
        }
        Long requiredTaskId = requireId(taskId, "Task id");

        Task task = taskRepository
                .findByIdAndDeletedFalseAndProjectDeletedFalse(requiredTaskId)
                .orElseThrow(TaskNotFoundException::new);
        validateTaskAccess(task);

        if (file.isEmpty()) {
            throw new EmptyFileException();
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File size must be <= 5 MB");
        }
        String mimeType = file.getContentType();
        if (!StringUtils.hasText(mimeType) || !ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new IllegalArgumentException("Unsupported file type");
        }

        String originalFileName = sanitizeFileName(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "_" + originalFileName;
        String filePath = UPLOAD_DIR + fileName;

        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
            file.transferTo(new File(filePath));
        } catch (IOException e) {
            throw new IllegalStateException("File upload failed");
        }

        Attachment attachment = new Attachment();
        attachment.setFileName(originalFileName);
        attachment.setFilePath(filePath);
        attachment.setMimeType(mimeType);
        attachment.setFileSize(file.getSize());
        attachment.setTask(task);
        attachment.setUploadedAt(LocalDateTime.now());
        attachment.setDeleted(false);

        Attachment savedAttachment = attachmentRepository.save(attachment);
        Attachment persistedAttachment = savedAttachment != null ? savedAttachment : attachment;
        cacheCoordinator.evictWorkspaceCaches();
        eventPublisher.publish(TaskManagementEvents.create(
                TaskManagementEntityType.ATTACHMENT,
                persistedAttachment.getId(),
                TaskManagementEventAction.UPLOADED,
                "Uploaded attachment " + persistedAttachment.getFileName()));
        return persistedAttachment;
    }

    @Override
    @PreAuthorize("hasAnyRole('TEAM_MEMBER', 'TEAM_LEADER', 'PROJECT_MANAGER', 'ADMIN')")
    public Attachment update(Long id, Attachment updatedAttachment) {
        if (updatedAttachment == null) {
            throw new IllegalArgumentException("Attachment details are required");
        }

        Attachment existingAttachment = findById(id);
        existingAttachment.setFileName(sanitizeFileName(updatedAttachment.getFileName()));
        Attachment savedAttachment = attachmentRepository.save(existingAttachment);
        Attachment persistedAttachment = savedAttachment != null ? savedAttachment : existingAttachment;
        cacheCoordinator.evictWorkspaceCaches();
        eventPublisher.publish(TaskManagementEvents.create(
                TaskManagementEntityType.ATTACHMENT,
                persistedAttachment.getId(),
                TaskManagementEventAction.UPDATED,
                "Renamed attachment " + persistedAttachment.getFileName()));
        return persistedAttachment;
    }

    @Override
    @PreAuthorize("hasAnyRole('TEAM_MEMBER', 'TEAM_LEADER', 'PROJECT_MANAGER', 'ADMIN')")
    public void softDelete(Long id) {
        Attachment attachment = findById(id);
        attachment.markAsDeleted();
        Attachment savedAttachment = attachmentRepository.save(attachment);
        Attachment persistedAttachment = savedAttachment != null ? savedAttachment : attachment;
        cacheCoordinator.evictWorkspaceCaches();
        eventPublisher.publish(TaskManagementEvents.create(
                TaskManagementEntityType.ATTACHMENT,
                persistedAttachment.getId(),
                TaskManagementEventAction.DELETED,
                "Deleted attachment " + persistedAttachment.getFileName()));
    }

    @Autowired(required = false)
    public void setEventPublisher(TaskManagementEventPublisher eventPublisher) {
        if (eventPublisher != null) {
            this.eventPublisher = eventPublisher;
        }
    }

    @Autowired
    public void setCacheCoordinator(TaskManagementCacheCoordinator cacheCoordinator) {
        if (cacheCoordinator != null) {
            this.cacheCoordinator = cacheCoordinator;
        }
    }

    private void validateTaskAccess(Task task) {
        if (SecurityUtils.hasAnyRole("ADMIN", "PROJECT_MANAGER", "TEAM_LEADER")) {
            return;
        }
        if (!isTaskAssignedToCurrentUser(task)) {
            throw new AccessDeniedException();
        }
    }

    private boolean isTaskAssignedToCurrentUser(Task task) {
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

    private String sanitizeFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "unnamed_file";
        }
        return fileName.trim().replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
    }

    private @NonNull Long requireId(@Nullable Long id, String fieldName) {
        return Objects.requireNonNull(id, fieldName + " is required");
    }
}
