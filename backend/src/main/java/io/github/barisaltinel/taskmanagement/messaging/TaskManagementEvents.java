package io.github.barisaltinel.taskmanagement.messaging;

import io.github.barisaltinel.taskmanagement.util.SecurityUtils;
import java.time.LocalDateTime;
import java.util.UUID;

public final class TaskManagementEvents {
  private TaskManagementEvents() {}

  public static TaskManagementEvent create(
      TaskManagementEntityType entityType,
      Long entityId,
      TaskManagementEventAction action,
      String summary) {
    TaskManagementEvent event = new TaskManagementEvent();
    event.setEventId(UUID.randomUUID().toString());
    event.setEntityType(entityType);
    event.setEntityId(entityId);
    event.setAction(action);
    event.setActor(SecurityUtils.getCurrentUsername());
    event.setSummary(summary);
    event.setOccurredAt(LocalDateTime.now());
    return event;
  }

  public static TaskManagementEvent create(
      TaskManagementEntityType entityType,
      Long entityId,
      TaskManagementEventAction action,
      String actor,
      String summary) {
    TaskManagementEvent event = create(entityType, entityId, action, summary);
    event.setActor(actor);
    return event;
  }
}
