package io.github.barisaltinel.taskmanagement.exception;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException() {
        super("Comment not found!");
    }
}
