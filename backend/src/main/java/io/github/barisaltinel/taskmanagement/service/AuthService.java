package io.github.barisaltinel.taskmanagement.service;

import io.github.barisaltinel.taskmanagement.model.User;
import java.time.LocalDateTime;

public interface AuthService {

  AuthSession login(String email, String password);

  User authenticate(String rawToken);

  void logout(String rawToken);

  record AuthSession(String token, LocalDateTime expiresAt, User user) {}
}
