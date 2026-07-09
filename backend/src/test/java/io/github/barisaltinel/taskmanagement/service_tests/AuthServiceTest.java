package io.github.barisaltinel.taskmanagement.service_tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.barisaltinel.taskmanagement.messaging.TaskManagementEventPublisher;
import io.github.barisaltinel.taskmanagement.model.AuthToken;
import io.github.barisaltinel.taskmanagement.model.User;
import io.github.barisaltinel.taskmanagement.repository.AuthTokenRepository;
import io.github.barisaltinel.taskmanagement.repository.UserRepository;
import io.github.barisaltinel.taskmanagement.service.AuthService;
import io.github.barisaltinel.taskmanagement.service.impl.AuthServiceImpl;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthTokenRepository authTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TaskManagementEventPublisher eventPublisher;

    @InjectMocks
    private AuthServiceImpl authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Baris Altinel");
        mockUser.setEmail("baris@example.com");
        mockUser.setPassword("encoded-password");
        mockUser.setRole("ADMIN");
        mockUser.setDeleted(false);
        authService.setEventPublisher(eventPublisher);
    }

    @Test
    void shouldLoginWithValidCredentials() {
        when(userRepository.findByEmailIgnoreCaseAndDeletedFalse("baris@example.com"))
                .thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("StrongPassword123", "encoded-password")).thenReturn(true);
        when(authTokenRepository.save(any(AuthToken.class))).thenAnswer(invocation -> {
            AuthToken token = invocation.getArgument(0);
            token.setId(99L);
            return token;
        });

        AuthService.AuthSession session = authService.login("baris@example.com", "StrongPassword123");

        assertThat(session.token()).isNotBlank();
        assertThat(session.user()).isEqualTo(mockUser);
        assertThat(session.expiresAt()).isAfter(LocalDateTime.now().plusHours(11));

        ArgumentCaptor<AuthToken> tokenCaptor = ArgumentCaptor.forClass(AuthToken.class);
        verify(authTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getTokenHash()).hasSize(64);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldRejectLoginWhenCredentialsAreBlank() {
        assertThatThrownBy(() -> authService.login("", ""))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Email or password is incorrect");
    }

    @Test
    void shouldRejectLoginWhenPasswordDoesNotMatch() {
        when(userRepository.findByEmailIgnoreCaseAndDeletedFalse("baris@example.com"))
                .thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("baris@example.com", "wrong-password"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Email or password is incorrect");
    }

    @Test
    void shouldAuthenticateActiveToken() {
        AuthToken authToken = new AuthToken();
        authToken.setUser(mockUser);
        authToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        when(authTokenRepository.findByTokenHashAndRevokedFalse(any(String.class)))
                .thenReturn(Optional.of(authToken));

        User authenticatedUser = authService.authenticate("raw-token");

        assertThat(authenticatedUser).isEqualTo(mockUser);
    }

    @Test
    void shouldRevokeExpiredTokenDuringAuthentication() {
        AuthToken expiredToken = new AuthToken();
        expiredToken.setUser(mockUser);
        expiredToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(authTokenRepository.findByTokenHashAndRevokedFalse(any(String.class)))
                .thenReturn(Optional.of(expiredToken));

        User authenticatedUser = authService.authenticate("raw-token");

        assertThat(authenticatedUser).isNull();
        assertThat(expiredToken.isRevoked()).isTrue();
    }

    @Test
    void shouldLogoutAndRevokeActiveToken() {
        AuthToken authToken = new AuthToken();
        authToken.setId(55L);
        authToken.setUser(mockUser);
        authToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        when(authTokenRepository.findByTokenHashAndRevokedFalse(any(String.class)))
                .thenReturn(Optional.of(authToken));

        authService.logout("raw-token");

        assertThat(authToken.isRevoked()).isTrue();
        verify(eventPublisher).publish(any());
    }
}
