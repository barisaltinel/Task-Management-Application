package io.github.barisaltinel.taskmanagement.service.impl;

import io.github.barisaltinel.taskmanagement.cache.TaskManagementCacheCoordinator;
import io.github.barisaltinel.taskmanagement.cache.TaskManagementCacheNames;
import io.github.barisaltinel.taskmanagement.exception.UserNotFoundException;
import io.github.barisaltinel.taskmanagement.messaging.TaskManagementEntityType;
import io.github.barisaltinel.taskmanagement.messaging.TaskManagementEventAction;
import io.github.barisaltinel.taskmanagement.messaging.TaskManagementEventPublisher;
import io.github.barisaltinel.taskmanagement.messaging.TaskManagementEvents;
import io.github.barisaltinel.taskmanagement.model.User;
import io.github.barisaltinel.taskmanagement.repository.UserRepository;
import io.github.barisaltinel.taskmanagement.service.UserService;
import io.github.barisaltinel.taskmanagement.util.SecurityUtils;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {
  private static final Set<String> ALLOWED_ROLES =
      Set.of("ADMIN", "PROJECT_MANAGER", "TEAM_LEADER", "TEAM_MEMBER");
  private static final String DEFAULT_ROLE = "TEAM_MEMBER";

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private TaskManagementEventPublisher eventPublisher = TaskManagementEventPublisher.noOp();
  private TaskManagementCacheCoordinator cacheCoordinator = TaskManagementCacheCoordinator.noOp();

  public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Cacheable(
      cacheNames = TaskManagementCacheNames.USER_LIST,
      key =
          "T(io.github.barisaltinel.taskmanagement.cache.TaskManagementCacheKeys).currentAccessScope()")
  public List<User> getAllUsers() {
    return userRepository.findAllByDeletedFalseOrderByIdAsc();
  }

  @Override
  @Cacheable(
      cacheNames = TaskManagementCacheNames.USER_DETAILS,
      key = "T(io.github.barisaltinel.taskmanagement.cache.TaskManagementCacheKeys).scopedId(#id)")
  public User findById(Long id) {
    Long requiredId = requireId(id, "User id");

    return userRepository
        .findByIdAndDeletedFalse(requiredId)
        .orElseThrow(UserNotFoundException::new);
  }

  @Override
  public User create(User user) {
    if (user == null) {
      throw new IllegalArgumentException("User details are required");
    }

    validateAndPrepareForSave(user, false);
    User savedUser = userRepository.save(user);
    User persistedUser = savedUser != null ? savedUser : user;
    evictCachesAfterUserWrite();
    eventPublisher.publish(
        TaskManagementEvents.create(
            TaskManagementEntityType.USER,
            persistedUser.getId(),
            TaskManagementEventAction.CREATED,
            "Created user " + persistedUser.getEmail()));
    return persistedUser;
  }

  @Override
  public User register(User user) {
    if (user == null) {
      throw new IllegalArgumentException("User details are required");
    }

    user.setRole(DEFAULT_ROLE);
    validateAndPrepareForSave(user, true);
    User savedUser = userRepository.save(user);
    User persistedUser = savedUser != null ? savedUser : user;
    evictCachesAfterUserWrite();
    eventPublisher.publish(
        TaskManagementEvents.create(
            TaskManagementEntityType.USER,
            persistedUser.getId(),
            TaskManagementEventAction.REGISTERED,
            persistedUser.getEmail(),
            "Registered a new account"));
    return persistedUser;
  }

  @Override
  public User update(Long id, User updatedUser) {
    if (updatedUser == null) {
      throw new IllegalArgumentException("User details are required");
    }
    if (!StringUtils.hasText(updatedUser.getName())) {
      throw new IllegalArgumentException("Name cannot be empty");
    }
    if (!StringUtils.hasText(updatedUser.getEmail())) {
      throw new IllegalArgumentException("Email cannot be empty");
    }

    User existingUser = findById(id);
    String updatedEmail = normalizeEmail(updatedUser.getEmail());
    if (!existingUser.getEmail().equalsIgnoreCase(updatedEmail)
        && userRepository.existsByEmailIgnoreCaseAndDeletedFalse(updatedEmail)) {
      throw new IllegalArgumentException("Email already in use");
    }
    existingUser.setName(updatedUser.getName().trim());
    existingUser.setEmail(updatedEmail);

    if (SecurityUtils.hasRole("ADMIN") && StringUtils.hasText(updatedUser.getRole())) {
      existingUser.setRole(normalizeAndValidateRole(updatedUser.getRole()));
    }

    if (StringUtils.hasText(updatedUser.getPassword())) {
      existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
    }

    User savedUser = userRepository.save(existingUser);
    User persistedUser = savedUser != null ? savedUser : existingUser;
    evictCachesAfterUserWrite();
    eventPublisher.publish(
        TaskManagementEvents.create(
            TaskManagementEntityType.USER,
            persistedUser.getId(),
            TaskManagementEventAction.UPDATED,
            "Updated user " + persistedUser.getEmail()));
    return persistedUser;
  }

  @Override
  public void softDelete(Long id) {
    User user = findById(id);
    user.softDelete();
    User savedUser = userRepository.save(user);
    User persistedUser = savedUser != null ? savedUser : user;
    evictCachesAfterUserWrite();
    eventPublisher.publish(
        TaskManagementEvents.create(
            TaskManagementEntityType.USER,
            persistedUser.getId(),
            TaskManagementEventAction.DELETED,
            "Archived user " + persistedUser.getEmail()));
  }

  @Autowired(required = false)
  public void setEventPublisher(TaskManagementEventPublisher eventPublisher) {
    if (eventPublisher != null) {
      this.eventPublisher = eventPublisher;
    }
  }

  @Autowired
  public void setCacheCoordinator(TaskManagementCacheCoordinator cacheCoordinator) {
    if (cacheCoordinator != null) {
      this.cacheCoordinator = cacheCoordinator;
    }
  }

  private void evictCachesAfterUserWrite() {
    cacheCoordinator.evictUserCaches();
    cacheCoordinator.evictWorkspaceCaches();
  }

  private void validateAndPrepareForSave(User user, boolean forceDefaultRole) {
    if (!StringUtils.hasText(user.getPassword())) {
      throw new IllegalArgumentException("Password cannot be empty");
    }
    if (!StringUtils.hasText(user.getName())) {
      throw new IllegalArgumentException("Name cannot be empty");
    }
    if (!StringUtils.hasText(user.getEmail())) {
      throw new IllegalArgumentException("Email cannot be empty");
    }
    String normalizedEmail = normalizeEmail(user.getEmail());
    if (userRepository.existsByEmailIgnoreCaseAndDeletedFalse(normalizedEmail)) {
      throw new IllegalArgumentException("Email already in use");
    }

    if (forceDefaultRole) {
      user.setRole(DEFAULT_ROLE);
    } else {
      user.setRole(normalizeAndValidateRole(user.getRole()));
    }

    user.setName(user.getName().trim());
    user.setEmail(normalizedEmail);
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    user.setDeleted(false);
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private String normalizeAndValidateRole(String role) {
    String normalizedRole = StringUtils.hasText(role) ? role.trim().toUpperCase() : DEFAULT_ROLE;
    if (!ALLOWED_ROLES.contains(normalizedRole)) {
      throw new IllegalArgumentException("Invalid role value");
    }
    return normalizedRole;
  }

  private Long requireId(Long id, String fieldName) {
    return Objects.requireNonNull(id, fieldName + " is required");
  }
}
