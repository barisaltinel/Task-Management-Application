package io.github.barisaltinel.taskmanagement.exception;

public class EmptyFileException extends RuntimeException {
    public EmptyFileException() {
        super("Cannot upload an empty file.");
    }
}


