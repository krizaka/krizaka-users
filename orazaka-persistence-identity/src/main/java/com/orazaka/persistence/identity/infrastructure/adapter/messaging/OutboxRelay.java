package com.orazaka.persistence.identity.infrastructure.adapter.messaging;

import com.orazaka.persistence.identity.domain.model.PendingOutboxEvent;
import com.orazaka.persistence.identity.domain.ports.OutboxStore;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * Drains the identity outbox to RabbitMQ (AGENTS.md §6): publishes each locked row with its {@code
 * messageId} as the AMQP message id and marks it published in the same transaction; a failed
 * publish backs off exponentially. During the dual-run window both hosts poll the table — {@code
 * FOR UPDATE SKIP LOCKED} plus consumer dedup make the overlap safe.
 */
@Component("identityOutboxRelay")
@ConditionalOnClass(RabbitTemplate.class)
class OutboxRelay {

  private static final Logger logger = LoggerFactory.getLogger(OutboxRelay.class);
  private static final int BATCH_SIZE = 100;
  private static final long PUBLISHED_RETENTION_DAYS = 7;

  private final OutboxStore outboxStore;
  private final RabbitTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;

  OutboxRelay(OutboxStore outboxStore, RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
    this.outboxStore = Objects.requireNonNull(outboxStore, "OutboxStore cannot be null");
    this.rabbitTemplate = Objects.requireNonNull(rabbitTemplate, "RabbitTemplate cannot be null");
    this.objectMapper = Objects.requireNonNull(objectMapper, "ObjectMapper cannot be null");
  }

  @Scheduled(fixedDelayString = "${orazaka.identity.outbox.poll-interval-ms:500}")
  @Transactional
  public void relayPendingBatch() {
    for (PendingOutboxEvent event : outboxStore.lockPendingBatch(BATCH_SIZE)) {
      try {
        rabbitTemplate.send(event.exchange(), event.routingKey(), toAmqpMessage(event));
        outboxStore.markPublished(event.id());
      } catch (RuntimeException e) {
        logger.warn(
            "Identity outbox publish failed for event {} ({} -> {}), attempt {} — backing off",
            event.id(),
            event.exchange(),
            event.routingKey(),
            event.attempts() + 1,
            e);
        outboxStore.recordFailure(event.id(), event.attempts());
      }
    }
  }

  /** Hourly housekeeping: drops published rows older than the retention window. */
  @Scheduled(fixedDelayString = "${orazaka.identity.outbox.purge-interval-ms:3600000}")
  public void purgePublished() {
    long purged =
        outboxStore.purgePublishedBefore(
            Instant.now().minus(PUBLISHED_RETENTION_DAYS, ChronoUnit.DAYS));
    if (purged > 0) {
      logger.info("Purged {} published identity outbox events", purged);
    }
  }

  private Message toAmqpMessage(PendingOutboxEvent event) {
    MessageProperties properties = new MessageProperties();
    properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
    properties.setMessageId(event.messageId().toString());
    properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
    // spring-amqp 4.0.0 SimpleAmqpHeaderMapper.toHeaders NPEs on a null priority.
    properties.setPriority(0);
    return new Message(objectMapper.writeValueAsBytes(event.payload()), properties);
  }
}
