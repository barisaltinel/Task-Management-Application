package io.github.barisaltinel.taskmanagement.controller;

import io.github.barisaltinel.taskmanagement.dto.ApiDtos;
import io.github.barisaltinel.taskmanagement.dto.ApiMapper;
import io.github.barisaltinel.taskmanagement.exception.UserNotFoundException;
import io.github.barisaltinel.taskmanagement.model.User;
import io.github.barisaltinel.taskmanagement.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<ApiDtos.UserResponse>> getAllUsers() {
    return ResponseEntity.ok(
        userService.getAllUsers().stream().map(ApiMapper::toUserResponse).toList());
  }

  @GetMapping("/{id}")
  @PreAuthorize(
      "hasRole('ADMIN') or authentication.principal.username == @userService.findById(#id).email")
  public ResponseEntity<ApiDtos.UserResponse> getUserById(@PathVariable Long id) {
    return ResponseEntity.ok(ApiMapper.toUserResponse(userService.findById(id)));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiDtos.UserResponse> createUser(
      @Valid @RequestBody ApiDtos.UserCreateRequest request) {
    User createdUser = userService.create(ApiMapper.toUser(request));
    return new ResponseEntity<>(ApiMapper.toUserResponse(createdUser), HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @PreAuthorize(
      "hasRole('ADMIN') or authentication.principal.username == @userService.findById(#id).email")
  public ResponseEntity<ApiDtos.UserResponse> updateUser(
      @PathVariable Long id, @Valid @RequestBody ApiDtos.UserUpdateRequest request) {
    User updatedUser = userService.update(id, ApiMapper.toUser(request));
    return ResponseEntity.ok(ApiMapper.toUserResponse(updatedUser));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> softDeleteUser(@PathVariable Long id) {
    userService.softDelete(id);
    return ResponseEntity.noContent().build();
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
  }
}
