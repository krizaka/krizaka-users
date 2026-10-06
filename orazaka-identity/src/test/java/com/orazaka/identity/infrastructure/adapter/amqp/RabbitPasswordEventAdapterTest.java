package com.orazaka.identity.infrastructure.adapter.amqp;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import com.orazaka.identity.domain.model.PasswordResetRequestedEvent;
import com.orazaka.persistence.identity.domain.model.OutboxMessage;
import com.orazaka.persistence.identity.domain.ports.OutboxStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RabbitPasswordEventAdapterTest {

  @Mock private OutboxStore outboxStore;

  private RabbitPasswordEventAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new RabbitPasswordEventAdapter(outboxStore);
  }

  @Test
  void shouldPublishEvent() {
    PasswordResetRequestedEvent event =
        new PasswordResetRequestedEvent("test@orazaka.com", "token123");

    adapter.publish(event);

    verify(outboxStore)
        .append(
            new OutboxMessage(
                "password", "test@orazaka.com", "orazaka.events", "evt.password.reset", event));
  }

  @Test
  void shouldThrowWhenEventIsNull() {
    assertThrows(NullPointerException.class, () -> adapter.publish(null));
  }
}
