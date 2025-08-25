package me.civka.monopoly.security;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.repository.ChatRepository;
import me.civka.monopoly.repository.entity.Chat;
import me.civka.monopoly.repository.entity.User;
import me.civka.monopoly.service.AuthService;
import me.civka.monopoly.service.exception.chat.ChatNotFoundException;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String CHAT_DESTINATION_PREFIX = "/topic/chats/";

  private final AuthService authService;
  private final ChatRepository chatRepository;

  @Override
  public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
    if (accessor.getCommand() == StompCommand.SUBSCRIBE) {

      String destination = accessor.getDestination();
      if (destination != null && destination.startsWith(CHAT_DESTINATION_PREFIX)) {
        UUID chatReference;
        try {
          chatReference = UUID.fromString(destination.substring(CHAT_DESTINATION_PREFIX.length()));
        } catch (IllegalArgumentException e) {
          return null;
        }

        Chat chat =
            chatRepository
                .findById(chatReference)
                .orElseThrow(() -> new ChatNotFoundException(chatReference));
        if (!chat.isPublic()) {
          Authentication authentication =
              authService.authenticate(accessor.getFirstNativeHeader(AUTHORIZATION_HEADER));
          if (authentication == null || chat.isUserAbsent((User) authentication.getPrincipal())) {
            return null;
          }
        }
      }
    }

    return message;
  }
}
