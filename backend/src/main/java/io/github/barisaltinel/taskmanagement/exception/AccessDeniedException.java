package io.github.barisaltinel.taskmanagement.exception;

public class AccessDeniedException extends RuntimeException {
  public AccessDeniedException() {
    super("You do not have permission to perform this action!");
  }

  public AccessDeniedException(String message) {
    super(message);
  }
}
