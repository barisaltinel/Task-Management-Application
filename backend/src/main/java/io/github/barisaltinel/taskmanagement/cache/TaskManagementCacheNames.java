package io.github.barisaltinel.taskmanagement.cache;

import java.util.List;

public final class TaskManagementCacheNames {
  public static final String PROJECT_LIST = "projects:list";
  public static final String PROJECT_DETAILS = "projects:details";
  public static final String TASK_LIST = "tasks:list";
  public static final String TASK_DETAILS = "tasks:details";
  public static final String COMMENT_LIST = "comments:list";
  public static final String COMMENT_DETAILS = "comments:details";
  public static final String ATTACHMENT_LIST = "attachments:list";
  public static final String ATTACHMENT_DETAILS = "attachments:details";
  public static final String USER_LIST = "users:list";
  public static final String USER_DETAILS = "users:details";

  private TaskManagementCacheNames() {}

  public static List<String> all() {
    return List.of(
        PROJECT_LIST,
        PROJECT_DETAILS,
        TASK_LIST,
        TASK_DETAILS,
        COMMENT_LIST,
        COMMENT_DETAILS,
        ATTACHMENT_LIST,
        ATTACHMENT_DETAILS,
        USER_LIST,
        USER_DETAILS);
  }

  public static List<String> workspace() {
    return List.of(
        PROJECT_LIST,
        PROJECT_DETAILS,
        TASK_LIST,
        TASK_DETAILS,
        COMMENT_LIST,
        COMMENT_DETAILS,
        ATTACHMENT_LIST,
        ATTACHMENT_DETAILS);
  }
}
