package com.orazaka.identity.infrastructure.adapter.amqp;

import com.orazaka.identity.domain.model.PasswordResetRequestedEvent;
import com.orazaka.identity.domain.ports.outbound.PasswordEventPublisher;
import com.orazaka.persistence.identity.domain.model.OutboxMessage;
import com.orazaka.persistence.identity.domain.ports.OutboxStore;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AMQP adapter for publishing password reset events downstream (AGENTS.md §6: {@code
 * evt.password.reset} on {@code orazaka.events}). Lives in the identity module — the publisher of
 * an identity domain event belongs to the identity context, not to persistence-app.
 *
 * <p>Duplicates the exchange/key it publishes (no shared messaging jar across the future service
 * boundary); the AMQP contract tests keep the copy honest. Published through the identity outbox
 * (at-least-once + messageId dedup).
 */
@Component
class RabbitPasswordEventAdapter implements PasswordEventPublisher {

  private static final Logger logger = LoggerFactory.getLogger(RabbitPasswordEventAdapter.class);
  private static final String EVENTS_EXCHANGE = "orazaka.events";
  private static final String EVT_PASSWORD_RESET = "evt.password.reset";

  private final OutboxStore outboxStore;

  RabbitPasswordEventAdapter(OutboxStore outboxStore) {
    this.outboxStore = Objects.requireNonNull(outboxStore, "OutboxStore cannot be null");
  }

  @Override
  public void publish(PasswordResetRequestedEvent event) {
    Objects.requireNonNull(event, "PasswordResetRequestedEvent cannot be null");
    logger.info("Publishing evt.password.reset event downstream for email: {}", event.email());
    outboxStore.append(
        new OutboxMessage("password", event.email(), EVENTS_EXCHANGE, EVT_PASSWORD_RESET, event));
  }
}
