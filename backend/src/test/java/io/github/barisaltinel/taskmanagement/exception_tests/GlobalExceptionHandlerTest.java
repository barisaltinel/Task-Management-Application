package io.github.barisaltinel.taskmanagement.exception_tests;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.barisaltinel.taskmanagement.exception.GlobalExceptionHandler;
import io.github.barisaltinel.taskmanagement.exception.TaskNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void shouldHandleTaskNotFoundException() {
        TaskNotFoundException exception = new TaskNotFoundException();
        ResponseEntity<String> response = exceptionHandler.handleTaskNotFoundException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Task not found!");
    }
}
