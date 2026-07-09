package io.github.barisaltinel.taskmanagement.exception;

public class TaskCannotBeModifiedException extends RuntimeException {
  public TaskCannotBeModifiedException() {
    super("Completed tasks cannot be modified!");
  }
}
