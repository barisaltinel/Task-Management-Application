package io.github.barisaltinel.taskmanagement.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@ConditionalOnProperty(prefix = "app.rabbitmq", name = "enabled", havingValue = "true")
public class RabbitTaskManagementEventPublisher implements TaskManagementEventPublisher {
  private final RabbitTemplate rabbitTemplate;
  private final RabbitMessagingProperties properties;

  public RabbitTaskManagementEventPublisher(
      RabbitTemplate rabbitTemplate, RabbitMessagingProperties properties) {
    this.rabbitTemplate = rabbitTemplate;
    this.properties = properties;
  }

  @Override
  public void publish(TaskManagementEvent event) {
    if (event == null) {
      return;
    }

    Runnable sender =
        () ->
            rabbitTemplate.convertAndSend(
                properties.getExchange(), properties.getRoutingKey(), event);

    if (TransactionSynchronizationManager.isActualTransactionActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              sender.run();
            }
          });
      return;
    }

    sender.run();
  }
}
