package io.github.barisaltinel.taskmanagement.controller_tests;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.barisaltinel.taskmanagement.controller.SystemController;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class SystemControllerTest {

    private final SystemController systemController = new SystemController();

    @Test
    void shouldExposeAppInfoWithDocsAndAuthLinks() {
        ResponseEntity<Map<String, Object>> response = systemController.appInfo();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("status", "running");
        assertThat(response.getBody()).containsKey("auth");
        assertThat(response.getBody()).containsKey("docs");
    }
}
