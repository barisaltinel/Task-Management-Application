package io.github.barisaltinel.taskmanagement.dto;

import io.github.barisaltinel.taskmanagement.model.Attachment;
import io.github.barisaltinel.taskmanagement.model.Comment;
import io.github.barisaltinel.taskmanagement.model.Project;
import io.github.barisaltinel.taskmanagement.model.Task;
import io.github.barisaltinel.taskmanagement.model.User;
import java.util.Collections;
import java.util.List;

public final class ApiMapper {
  private ApiMapper() {}

  public static User toUser(ApiDtos.AuthRegisterRequest request) {
    User user = new User();
    user.setName(request.name());
    user.setEmail(request.email());
    user.setPassword(request.password());
    return user;
  }

  public static User toUser(ApiDtos.UserCreateRequest request) {
    User user = new User();
    user.setName(request.name());
    user.setEmail(request.email());
    user.setPassword(request.password());
    user.setRole(request.role());
    return user;
  }

  public static User toUser(ApiDtos.UserUpdateRequest request) {
    User user = new User();
    user.setName(request.name());
    user.setEmail(request.email());
    user.setPassword(request.password());
    user.setRole(request.role());
    return user;
  }

  public static Project toProject(ApiDtos.ProjectUpsertRequest request) {
    Project project = new Project();
    project.setTitle(request.title());
    project.setDescription(request.description());
    project.setDepartmentName(request.departmentName());
    project.setStatus(request.status());
    return project;
  }

  public static Task toTask(ApiDtos.TaskUpsertRequest request) {
    Task task = new Task();
    task.setTitle(request.title());
    task.setDescription(request.description());
    task.setPriority(request.priority());
    task.setState(request.state());
    return task;
  }

  public static ApiDtos.UserSummaryResponse toUserSummary(User user) {
    if (user == null) {
      return null;
    }

    return new ApiDtos.UserSummaryResponse(
        user.getId(), user.getName(), user.getEmail(), user.getRole());
  }

  public static ApiDtos.UserResponse toUserResponse(User user) {
    return new ApiDtos.UserResponse(
        user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getCreatedAt());
  }

  public static ApiDtos.ProjectSummaryResponse toProjectSummary(Project project) {
    if (project == null) {
      return null;
    }

    return new ApiDtos.ProjectSummaryResponse(
        project.getId(), project.getTitle(), project.getStatus());
  }

  public static ApiDtos.ProjectResponse toProjectResponse(Project project) {
    List<User> teamMembers =
        project.getTeamMembers() == null ? Collections.emptyList() : project.getTeamMembers();

    return new ApiDtos.ProjectResponse(
        project.getId(),
        project.getTitle(),
        project.getDescription(),
        project.getDepartmentName(),
        project.getStatus(),
        teamMembers.stream().map(ApiMapper::toUserSummary).toList());
  }

  public static ApiDtos.TaskResponse toTaskResponse(Task task) {
    return new ApiDtos.TaskResponse(
        task.getId(),
        task.getTitle(),
        task.getDescription(),
        task.getState(),
        task.getPriority(),
        task.getReason(),
        toProjectSummary(task.getProject()),
        toUserSummary(task.getAssignee()));
  }

  public static ApiDtos.CommentResponse toCommentResponse(Comment comment) {
    Long taskId = comment.getTask() != null ? comment.getTask().getId() : null;
    return new ApiDtos.CommentResponse(
        comment.getId(),
        comment.getText(),
        taskId,
        toUserSummary(comment.getAuthor()),
        comment.getCreatedAt());
  }

  public static ApiDtos.AttachmentResponse toAttachmentResponse(Attachment attachment) {
    Long taskId = attachment.getTask() != null ? attachment.getTask().getId() : null;
    return new ApiDtos.AttachmentResponse(
        attachment.getId(),
        attachment.getFileName(),
        attachment.getMimeType(),
        attachment.getFileSize(),
        taskId,
        attachment.getUploadedAt());
  }
}
