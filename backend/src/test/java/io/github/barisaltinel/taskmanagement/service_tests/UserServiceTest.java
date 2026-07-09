package io.github.barisaltinel.taskmanagement.service_tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.barisaltinel.taskmanagement.exception.UserNotFoundException;
import io.github.barisaltinel.taskmanagement.model.User;
import io.github.barisaltinel.taskmanagement.repository.UserRepository;
import io.github.barisaltinel.taskmanagement.service.impl.UserServiceImpl;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UserServiceImpl userService;

  private User mockUser;

  @BeforeEach
  void setUp() {
    mockUser = new User();
    mockUser.setId(1L);
    mockUser.setName("John Doe");
    mockUser.setEmail("john@example.com");
    mockUser.setPassword("plainPassword");
    mockUser.setRole("TEAM_MEMBER");
    mockUser.setDeleted(false);
  }

  @Test
  void shouldReturnAllUsers() {
    when(userRepository.findAllByDeletedFalseOrderByIdAsc()).thenReturn(List.of(mockUser));
    List<User> users = userService.getAllUsers();
    assertThat(users).hasSize(1);
    assertThat(users.get(0)).isEqualTo(mockUser);
  }

  @Test
  void shouldReturnUserById() {
    when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(mockUser));
    User user = userService.findById(1L);
    assertThat(user).isEqualTo(mockUser);
  }

  @Test
  void shouldThrowExceptionWhenUserNotFound() {
    when(userRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> userService.findById(99L))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found");
  }

  @Test
  void shouldCreateUser() {
    when(userRepository.existsByEmailIgnoreCaseAndDeletedFalse(mockUser.getEmail()))
        .thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(userRepository.save(anyUser()))
        .thenReturn(requireUser(mockUser, "mockUser must not be null"));

    User user = userService.create(mockUser);
    assertThat(user).isEqualTo(mockUser);
  }

  @Test
  void shouldRegisterUserWithDefaultRole() {
    when(userRepository.existsByEmailIgnoreCaseAndDeletedFalse(mockUser.getEmail()))
        .thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(userRepository.save(anyUser()))
        .thenReturn(requireUser(mockUser, "mockUser must not be null"));

    User user = userService.register(mockUser);
    assertThat(user).isEqualTo(mockUser);
  }

  @Test
  void shouldUpdateUser() {
    when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(mockUser));
    when(userRepository.existsByEmailIgnoreCaseAndDeletedFalse("updated@example.com"))
        .thenReturn(false);
    when(userRepository.save(anyUser()))
        .thenReturn(requireUser(mockUser, "mockUser must not be null"));

    User updatedUser = new User();
    updatedUser.setName("Updated Name");
    updatedUser.setEmail("updated@example.com");

    User result = userService.update(1L, updatedUser);
    assertThat(result).isEqualTo(mockUser);
  }

  @Test
  void shouldSoftDeleteUser() {
    when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(mockUser));
    userService.softDelete(1L);
    assertThat(mockUser.isDeleted()).isTrue();
    verify(userRepository, times(1)).save(requireUser(mockUser, "mockUser must not be null"));
  }

  private @NonNull User requireUser(@Nullable User user, String message) {
    return Objects.requireNonNull(user, message);
  }

  private @NonNull User anyUser() {
    return any(User.class);
  }
}
