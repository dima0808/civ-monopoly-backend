package me.civka.monopoly.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

  @EventListener
  public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    // TODO: Implement logic to handle WebSocket disconnect events if needed
  }
}
