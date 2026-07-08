package io.github.barisaltinel.taskmanagement.messaging;

public interface TaskManagementEventPublisher {
    void publish(TaskManagementEvent event);

    static TaskManagementEventPublisher noOp() {
        return NoOpTaskManagementEventPublisher.INSTANCE;
    }
}
