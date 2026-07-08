package io.github.barisaltinel.taskmanagement.messaging;

import java.io.Serializable;
import java.time.LocalDateTime;

public class TaskManagementEvent implements Serializable {
    private String eventId;
    private TaskManagementEntityType entityType;
    private Long entityId;
    private TaskManagementEventAction action;
    private String actor;
    private String summary;
    private LocalDateTime occurredAt;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public TaskManagementEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(TaskManagementEntityType entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public TaskManagementEventAction getAction() {
        return action;
    }

    public void setAction(TaskManagementEventAction action) {
        this.action = action;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}
