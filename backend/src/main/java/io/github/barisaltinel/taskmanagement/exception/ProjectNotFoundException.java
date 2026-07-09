package io.github.barisaltinel.taskmanagement.exception;

public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException() {
        super("Project not found!");
    }
}
