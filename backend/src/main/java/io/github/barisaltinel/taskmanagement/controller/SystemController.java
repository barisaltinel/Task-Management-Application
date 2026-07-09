package io.github.barisaltinel.taskmanagement.controller;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemController {

  @GetMapping({"/", "/api", "/api/public/app-info"})
  public ResponseEntity<Map<String, Object>> appInfo() {
    Map<String, Object> auth = new LinkedHashMap<>();
    auth.put("login", "POST /api/auth/login");
    auth.put("register", "POST /api/auth/register");

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("name", "Task Management Application API");
    payload.put("status", "running");
    payload.put("timestamp", OffsetDateTime.now());
    payload.put(
        "message", "Backend is available. Open the frontend app to sign in and use the workspace.");
    payload.put("auth", auth);

    return ResponseEntity.ok(payload);
  }
}
