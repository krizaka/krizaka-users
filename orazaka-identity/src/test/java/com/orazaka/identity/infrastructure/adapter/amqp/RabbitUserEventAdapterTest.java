package com.orazaka.identity.infrastructure.adapter.amqp;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import com.orazaka.identity.domain.model.User;
import com.orazaka.identity.domain.model.UserRegisteredEvent;
import com.orazaka.persistence.identity.domain.model.OutboxMessage;
import com.orazaka.persistence.identity.domain.ports.OutboxStore;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RabbitUserEventAdapterTest {

  @Mock private OutboxStore outboxStore;

  private RabbitUserEventAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new RabbitUserEventAdapter(outboxStore);
  }

  @Test
  void shouldPublishEvent() {
    User user =
        new User(
            UUID.randomUUID(), "testuser", "test@orazaka.com", true, Set.of("ROLE_USER"), Map.of());
    UserRegisteredEvent event = new UserRegisteredEvent(user, "token123");

    adapter.publish(event);

    verify(outboxStore)
        .append(
            new OutboxMessage(
                "user", user.id().toString(), "orazaka.events", "evt.user.registered", event));
  }

  @Test
  void shouldThrowWhenEventIsNull() {
    assertThrows(NullPointerException.class, () -> adapter.publish(null));
  }
}
