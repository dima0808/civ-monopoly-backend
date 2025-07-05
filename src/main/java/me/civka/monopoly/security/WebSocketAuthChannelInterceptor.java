package me.civka.monopoly.security;

import lombok.RequiredArgsConstructor;
import me.civka.monopoly.service.AuthService;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

  private final AuthService authService;

  @Override
  public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
    //    TODO: private and room chats access control
    //    if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
    //
    //      Authentication authentication =
    //          authService.authenticate(accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION));
    //
    //      if (authentication == null) {
    //        return null;
    //      }
    //    }

    return message;
  }
}
