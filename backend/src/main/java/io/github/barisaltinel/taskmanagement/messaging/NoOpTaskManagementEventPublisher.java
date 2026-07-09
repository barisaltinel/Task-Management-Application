package io.github.barisaltinel.taskmanagement.messaging;

public class NoOpTaskManagementEventPublisher implements TaskManagementEventPublisher {
  static final NoOpTaskManagementEventPublisher INSTANCE = new NoOpTaskManagementEventPublisher();

  @Override
  public void publish(TaskManagementEvent event) {}
}
