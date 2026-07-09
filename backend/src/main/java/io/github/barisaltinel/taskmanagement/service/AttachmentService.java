package io.github.barisaltinel.taskmanagement.service;

import io.github.barisaltinel.taskmanagement.exception.AttachmentNotFoundException;
import io.github.barisaltinel.taskmanagement.model.Attachment;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface AttachmentService {
  /** Returns the active attachments the current user can access. */
  List<Attachment> getAllAttachments();

  /** Returns one active attachment if the current user can access it. */
  Attachment findById(Long id) throws AttachmentNotFoundException;

  /** Uploads a new file for the given task. */
  Attachment upload(MultipartFile file, Long taskId);

  /** Updates attachment metadata. */
  Attachment update(Long id, Attachment updatedAttachment);

  /** Marks an attachment as deleted without removing the record. */
  void softDelete(Long id);
}
