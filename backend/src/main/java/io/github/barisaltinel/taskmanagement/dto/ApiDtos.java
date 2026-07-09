package io.github.barisaltinel.taskmanagement.dto;

import io.github.barisaltinel.taskmanagement.model.ProjectStatus;
import io.github.barisaltinel.taskmanagement.model.TaskPriority;
import io.github.barisaltinel.taskmanagement.model.TaskState;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class ApiDtos {
    private ApiDtos() {}

    public record AuthLoginRequest(
            @Email(message = "Email should be valid") @NotBlank(message = "Email cannot be empty") String email,
            @NotBlank(message = "Password cannot be empty") String password) {}

    public record AuthRegisterRequest(
            @NotBlank(message = "Name cannot be empty") String name,
            @Email(message = "Email should be valid") @NotBlank(message = "Email cannot be empty") String email,
            @NotBlank(message = "Password cannot be empty") String password) {}

    public record AuthResponse(String token, LocalDateTime expiresAt, UserResponse user) {}

    public record UserSummaryResponse(Long id, String name, String email, String role) {}

    public record UserResponse(Long id, String name, String email, String role, LocalDateTime createdAt) {}

    public record UserCreateRequest(
            @NotBlank(message = "Name cannot be empty") String name,
            @Email(message = "Email should be valid") @NotBlank(message = "Email cannot be empty") String email,
            @NotBlank(message = "Password cannot be empty") String password,
            @NotBlank(message = "Role cannot be empty") String role) {}

    public record UserUpdateRequest(
            @NotBlank(message = "Name cannot be empty") String name,
            @Email(message = "Email should be valid") @NotBlank(message = "Email cannot be empty") String email,
            String password,
            String role) {}

    public record ProjectSummaryResponse(Long id, String title, ProjectStatus status) {}

    public record ProjectUpsertRequest(
            @NotBlank(message = "Project title cannot be empty") String title,
            @NotBlank(message = "Project description cannot be empty") String description,
            @NotBlank(message = "Department name cannot be empty") String departmentName,
            @NotNull(message = "Project status is required") ProjectStatus status,
            List<Long> teamMemberIds) {}

    public record ProjectResponse(
            Long id,
            String title,
            String description,
            String departmentName,
            ProjectStatus status,
            List<UserSummaryResponse> teamMembers) {}

    public record TaskUpsertRequest(
            @NotBlank(message = "Title cannot be empty") String title,
            @NotBlank(message = "Description cannot be empty") String description,
            @NotNull(message = "Priority is required") TaskPriority priority,
            TaskState state,
            LocalDate startDate,
            LocalDate dueDate,
            @NotNull(message = "Project id is required") Long projectId,
            @NotNull(message = "Assignee id is required") Long assigneeId) {}

    public record TaskResponse(
            Long id,
            String title,
            String description,
            TaskState state,
            TaskPriority priority,
            LocalDate startDate,
            LocalDate dueDate,
            String reason,
            ProjectSummaryResponse project,
            UserSummaryResponse assignee) {}

    public record CommentCreateRequest(
            @NotNull(message = "Task id is required") Long taskId,
            @NotBlank(message = "Comment text cannot be empty") String text) {}

    public record CommentResponse(
            Long id, String text, Long taskId, UserSummaryResponse author, LocalDateTime createdAt) {}

    public record AttachmentResponse(
            Long id, String fileName, String mimeType, Long fileSize, Long taskId, LocalDateTime uploadedAt) {}
}
