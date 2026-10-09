package com.orazaka.persistence.identity.infrastructure.adapter.messaging;

import com.krizaka.messaging.outbox.OutboxMessage;
import com.orazaka.persistence.identity.domain.model.PendingOutboxEvent;
import com.orazaka.persistence.identity.domain.ports.OutboxStore;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Hands the identity outbox to the krizaka-messaging relay (AGENTS.md §6).
 *
 * <p>The relay — claim a batch, publish each row with its {@code messageId} as the AMQP message id,
 * mark it, back off on failure, purge after the retention window — is krizaka-messaging's, and runs
 * on its own scheduler. This adapter only translates: the context's {@link OutboxStore} keeps its
 * schema and its claim ({@code FOR UPDATE SKIP LOCKED}), and a row's {@code Map} payload is
 * serialised to JSON at the moment it leaves.
 */
@Component("identityRelayOutboxStoreAdapter")
@ConditionalOnClass(RabbitTemplate.class)
class RelayOutboxStoreAdapter implements com.krizaka.messaging.outbox.OutboxStore {

  private final OutboxStore outboxStore;
  private final ObjectMapper objectMapper;

  RelayOutboxStoreAdapter(OutboxStore outboxStore, ObjectMapper objectMapper) {
    this.outboxStore = Objects.requireNonNull(outboxStore, "OutboxStore cannot be null");
    this.objectMapper = Objects.requireNonNull(objectMapper, "ObjectMapper cannot be null");
  }

  @Override
  public List<OutboxMessage> lockPendingBatch(int batchSize) {
    return outboxStore.lockPendingBatch(batchSize).stream().map(this::toRelayed).toList();
  }

  @Override
  public void markPublished(UUID id) {
    outboxStore.markPublished(id);
  }

  @Override
  public void recordFailure(UUID id, int previousAttempts) {
    outboxStore.recordFailure(id, previousAttempts);
  }

  @Override
  public long purgePublishedBefore(Instant cutoff) {
    return outboxStore.purgePublishedBefore(cutoff);
  }

  private OutboxMessage toRelayed(PendingOutboxEvent event) {
    return new OutboxMessage(
        event.id(),
        event.exchange(),
        event.routingKey(),
        event.messageId().toString(),
        objectMapper.writeValueAsBytes(event.payload()),
        event.attempts());
  }
}
