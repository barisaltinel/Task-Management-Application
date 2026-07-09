package io.github.barisaltinel.taskmanagement.controller_tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import io.github.barisaltinel.taskmanagement.controller.UserController;
import io.github.barisaltinel.taskmanagement.dto.ApiDtos;
import io.github.barisaltinel.taskmanagement.exception.UserNotFoundException;
import io.github.barisaltinel.taskmanagement.model.User;
import io.github.barisaltinel.taskmanagement.service.UserService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  @Mock private UserService userService;

  @InjectMocks private UserController userController;

  private User mockUser;

  @BeforeEach
  void setUp() {
    mockUser = new User();
    mockUser.setId(1L);
    mockUser.setName("John Doe");
    mockUser.setEmail("john@example.com");
    mockUser.setRole("TEAM_MEMBER");
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void shouldReturnAllUsers() {
    when(userService.getAllUsers()).thenReturn(List.of(mockUser));
    ResponseEntity<List<ApiDtos.UserResponse>> response = userController.getAllUsers();
    assertThat(response.getBody()).isNotNull().hasSize(1);
    assertThat(response.getBody().get(0).email()).isEqualTo(mockUser.getEmail());
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void shouldReturnUserById() {
    when(userService.findById(1L)).thenReturn(mockUser);
    ResponseEntity<ApiDtos.UserResponse> response = userController.getUserById(1L);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().email()).isEqualTo(mockUser.getEmail());
  }

  @Test
  @WithMockUser(username = "john@example.com", roles = "TEAM_MEMBER")
  void shouldReturnUserForSelf() {
    when(userService.findById(1L)).thenReturn(mockUser);
    ResponseEntity<ApiDtos.UserResponse> response = userController.getUserById(1L);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().email()).isEqualTo(mockUser.getEmail());
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void shouldThrowExceptionWhenUserNotFound() {
    when(userService.findById(99L)).thenThrow(new UserNotFoundException());
    assertThatThrownBy(() -> userController.getUserById(99L))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found");
  }

  @Test
  void shouldCreateUser() {
    ApiDtos.UserCreateRequest request =
        new ApiDtos.UserCreateRequest(
            "John Doe", "john@example.com", "plainPassword", "TEAM_MEMBER");
    when(userService.create(any(User.class))).thenReturn(mockUser);
    ResponseEntity<ApiDtos.UserResponse> response = userController.createUser(request);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().email()).isEqualTo(mockUser.getEmail());
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void shouldUpdateUser() {
    ApiDtos.UserUpdateRequest request =
        new ApiDtos.UserUpdateRequest(
            "John Doe", "john@example.com", "plainPassword", "TEAM_MEMBER");
    when(userService.update(anyLong(), any(User.class))).thenReturn(mockUser);
    ResponseEntity<ApiDtos.UserResponse> response = userController.updateUser(1L, request);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().email()).isEqualTo(mockUser.getEmail());
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void shouldSoftDeleteUser() {
    doNothing().when(userService).softDelete(1L);
    ResponseEntity<Void> response = userController.softDeleteUser(1L);
    assertThat(response.getStatusCode().value()).isEqualTo(204);
  }
}
