package io.github.barisaltinel.taskmanagement.service.impl;

import io.github.barisaltinel.taskmanagement.messaging.TaskManagementEntityType;
import io.github.barisaltinel.taskmanagement.messaging.TaskManagementEventAction;
import io.github.barisaltinel.taskmanagement.messaging.TaskManagementEventPublisher;
import io.github.barisaltinel.taskmanagement.messaging.TaskManagementEvents;
import io.github.barisaltinel.taskmanagement.model.AuthToken;
import io.github.barisaltinel.taskmanagement.model.User;
import io.github.barisaltinel.taskmanagement.repository.AuthTokenRepository;
import io.github.barisaltinel.taskmanagement.repository.UserRepository;
import io.github.barisaltinel.taskmanagement.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final HexFormat HEX_FORMAT = HexFormat.of();
    private static final int TOKEN_BYTES = 32;
    private static final long SESSION_HOURS = 12;

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private TaskManagementEventPublisher eventPublisher = TaskManagementEventPublisher.noOp();

    public AuthServiceImpl(
            UserRepository userRepository,
            AuthTokenRepository authTokenRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.authTokenRepository = authTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public AuthSession login(String email, String password) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            throw new BadCredentialsException("Email or password is incorrect");
        }

        User user = userRepository.findByEmailIgnoreCaseAndDeletedFalse(email.trim())
                .orElseThrow(() -> new BadCredentialsException("Email or password is incorrect"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Email or password is incorrect");
        }

        String rawToken = generateToken();
        AuthToken authToken = new AuthToken();
        authToken.setTokenHash(hashToken(rawToken));
        authToken.setUser(user);
        authToken.setExpiresAt(LocalDateTime.now().plusHours(SESSION_HOURS));
        authToken.setRevoked(false);
        AuthToken savedToken = authTokenRepository.save(authToken);
        AuthToken persistedToken = savedToken != null ? savedToken : authToken;
        eventPublisher.publish(TaskManagementEvents.create(
                TaskManagementEntityType.AUTH_SESSION,
                persistedToken.getId(),
                TaskManagementEventAction.LOGGED_IN,
                user.getEmail(),
                "Started a new session"
        ));

        return new AuthSession(rawToken, persistedToken.getExpiresAt(), user);
    }

    @Override
    @Transactional
    public User authenticate(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            return null;
        }

        Optional<AuthToken> token = authTokenRepository.findByTokenHashAndRevokedFalse(hashToken(rawToken.trim()));
        if (token.isEmpty()) {
            return null;
        }

        AuthToken authToken = token.get();
        if (authToken.getExpiresAt().isBefore(LocalDateTime.now()) || authToken.getUser().isDeleted()) {
            authToken.setRevoked(true);
            return null;
        }

        return authToken.getUser();
    }

    @Override
    @Transactional
    public void logout(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            return;
        }

        authTokenRepository.findByTokenHashAndRevokedFalse(hashToken(rawToken.trim()))
                .ifPresent(token -> {
                    token.setRevoked(true);
                    User user = token.getUser();
                    String actor = user != null ? user.getEmail() : null;
                    eventPublisher.publish(TaskManagementEvents.create(
                            TaskManagementEntityType.AUTH_SESSION,
                            token.getId(),
                            TaskManagementEventAction.LOGGED_OUT,
                            actor,
                            "Closed an active session"
                    ));
                });
    }

    @Autowired(required = false)
    public void setEventPublisher(TaskManagementEventPublisher eventPublisher) {
        if (eventPublisher != null) {
            this.eventPublisher = eventPublisher;
        }
    }

    private String generateToken() {
        byte[] tokenBytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return HEX_FORMAT.formatHex(tokenBytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HEX_FORMAT.formatHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Could not hash token", ex);
        }
    }
}


