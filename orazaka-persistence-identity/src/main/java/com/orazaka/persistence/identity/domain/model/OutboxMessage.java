package com.orazaka.persistence.identity.domain.model;

import java.util.Objects;

/**
 * Immutable request to append a message to the identity outbox (AGENTS.md §6): committed with the
 * caller's transaction and published asynchronously by the identity relay. Context-local copy of
 * the platform outbox contract — no shared messaging jar across the service boundary.
 *
 * @param aggregateType the aggregate the message belongs to (e.g. {@code user})
 * @param aggregateId the aggregate identifier
 * @param exchange the target exchange ({@code orazaka.events})
 * @param routingKey the routing key ({@code evt.{aggregate}.{type}})
 * @param payload the message payload, serialized to JSON by the store
 */
public record OutboxMessage(
    String aggregateType, String aggregateId, String exchange, String routingKey, Object payload) {

  public OutboxMessage {
    requireNonBlank(aggregateType, "aggregateType");
    requireNonBlank(aggregateId, "aggregateId");
    requireNonBlank(exchange, "exchange");
    requireNonBlank(routingKey, "routingKey");
    Objects.requireNonNull(payload, "payload cannot be null");
  }

  private static void requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " cannot be blank");
    }
  }
}
