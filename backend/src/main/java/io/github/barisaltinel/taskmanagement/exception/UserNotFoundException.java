package io.github.barisaltinel.taskmanagement.exception;

public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException() {
    super("User not found!");
  }
}
