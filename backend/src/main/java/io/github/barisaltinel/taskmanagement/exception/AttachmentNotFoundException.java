package io.github.barisaltinel.taskmanagement.exception;

public class AttachmentNotFoundException extends RuntimeException {
  public AttachmentNotFoundException() {
    super("Attachment not found!");
  }
}
