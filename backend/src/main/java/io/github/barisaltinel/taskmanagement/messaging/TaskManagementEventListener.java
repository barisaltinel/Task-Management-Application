package io.github.barisaltinel.taskmanagement.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.rabbitmq", name = "enabled", havingValue = "true")
public class TaskManagementEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskManagementEventListener.class);

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void handle(TaskManagementEvent event) {
        if (event == null) {
            return;
        }

        LOGGER.info(
                "Processed domain event {} {}#{} by {}",
                event.getAction(),
                event.getEntityType(),
                event.getEntityId(),
                event.getActor()
        );
    }
}
