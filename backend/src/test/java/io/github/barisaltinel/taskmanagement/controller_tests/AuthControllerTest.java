package io.github.barisaltinel.taskmanagement.controller_tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.barisaltinel.taskmanagement.controller.AuthController;
import io.github.barisaltinel.taskmanagement.dto.ApiDtos;
import io.github.barisaltinel.taskmanagement.model.User;
import io.github.barisaltinel.taskmanagement.service.AuthService;
import io.github.barisaltinel.taskmanagement.service.UserService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Baris Altinel");
        mockUser.setEmail("baris@example.com");
        mockUser.setPassword("encoded-password");
        mockUser.setRole("ADMIN");
        mockUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void shouldRegisterUser() {
        ApiDtos.AuthRegisterRequest request =
                new ApiDtos.AuthRegisterRequest("Baris Altinel", "baris@example.com", "StrongPassword123");
        when(userService.register(any(User.class))).thenReturn(mockUser);

        ResponseEntity<ApiDtos.UserResponse> response = authController.registerUser(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().email()).isEqualTo("baris@example.com");
        verify(userService).register(any(User.class));
    }

    @Test
    void shouldLoginUser() {
        ApiDtos.AuthLoginRequest request = new ApiDtos.AuthLoginRequest("baris@example.com", "StrongPassword123");
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(12);
        when(authService.login("baris@example.com", "StrongPassword123"))
                .thenReturn(new AuthService.AuthSession("token-123", expiresAt, mockUser));

        ResponseEntity<ApiDtos.AuthResponse> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isEqualTo("token-123");
        assertThat(response.getBody().user().email()).isEqualTo("baris@example.com");
    }

    @Test
    void shouldLogoutWithBearerToken() {
        ResponseEntity<Void> response = authController.logout("Bearer token-123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(authService).logout("token-123");
    }

    @Test
    void shouldLogoutWithNullTokenWhenHeaderIsInvalid() {
        ResponseEntity<Void> response = authController.logout("Basic token-123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(authService).logout(null);
    }
}
