package io.github.barisaltinel.taskmanagement.controller;

import io.github.barisaltinel.taskmanagement.dto.ApiDtos;
import io.github.barisaltinel.taskmanagement.dto.ApiMapper;
import io.github.barisaltinel.taskmanagement.exception.AttachmentNotFoundException;
import io.github.barisaltinel.taskmanagement.model.Attachment;
import io.github.barisaltinel.taskmanagement.service.AttachmentService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {
    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TEAM_MEMBER', 'TEAM_LEADER', 'PROJECT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<ApiDtos.AttachmentResponse>> getAllAttachments() {
        return ResponseEntity.ok(attachmentService.getAllAttachments().stream()
                .map(ApiMapper::toAttachmentResponse)
                .toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEAM_MEMBER', 'TEAM_LEADER', 'PROJECT_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiDtos.AttachmentResponse> getAttachmentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiMapper.toAttachmentResponse(attachmentService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEAM_MEMBER', 'TEAM_LEADER', 'PROJECT_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiDtos.AttachmentResponse> uploadAttachment(
            @RequestParam("file") MultipartFile file, @RequestParam("taskId") Long taskId) {
        Attachment attachment = attachmentService.upload(file, taskId);
        return new ResponseEntity<>(ApiMapper.toAttachmentResponse(attachment), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEAM_MEMBER', 'TEAM_LEADER', 'PROJECT_MANAGER', 'ADMIN')")
    public ResponseEntity<Void> softDeleteAttachment(@PathVariable Long id) {
        attachmentService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(AttachmentNotFoundException.class)
    public ResponseEntity<String> handleAttachmentNotFound(AttachmentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
