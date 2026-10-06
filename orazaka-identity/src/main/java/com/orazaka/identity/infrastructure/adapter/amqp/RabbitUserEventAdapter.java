package com.orazaka.identity.infrastructure.adapter.amqp;

import com.orazaka.identity.domain.model.UserRegisteredEvent;
import com.orazaka.identity.domain.ports.outbound.UserEventPublisher;
import com.orazaka.persistence.identity.domain.model.OutboxMessage;
import com.orazaka.persistence.identity.domain.ports.OutboxStore;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AMQP adapter for publishing user registration events downstream (AGENTS.md §6: {@code
 * evt.user.registered} on {@code orazaka.events}). Lives in the identity module — the publisher of
 * an identity domain event belongs to the identity context, not to persistence-app.
 *
 * <p>Duplicates the exchange/key it publishes (no shared messaging jar across the future service
 * boundary); the AMQP contract tests keep the copy honest. Published through the identity outbox
 * (at-least-once + messageId dedup).
 */
@Component
class RabbitUserEventAdapter implements UserEventPublisher {

  private static final Logger logger = LoggerFactory.getLogger(RabbitUserEventAdapter.class);
  private static final String EVENTS_EXCHANGE = "orazaka.events";
  private static final String EVT_USER_REGISTERED = "evt.user.registered";

  private final OutboxStore outboxStore;

  RabbitUserEventAdapter(OutboxStore outboxStore) {
    this.outboxStore = Objects.requireNonNull(outboxStore, "OutboxStore cannot be null");
  }

  @Override
  public void publish(UserRegisteredEvent event) {
    Objects.requireNonNull(event, "UserRegisteredEvent cannot be null");
    logger.info(
        "Publishing evt.user.registered event downstream for user: {}", event.user().email());
    outboxStore.append(
        new OutboxMessage(
            "user", event.user().id().toString(), EVENTS_EXCHANGE, EVT_USER_REGISTERED, event));
  }
}
