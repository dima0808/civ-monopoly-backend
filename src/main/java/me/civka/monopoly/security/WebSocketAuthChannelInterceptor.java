package me.civka.monopoly.security;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.repository.ChatRepository;
import me.civka.monopoly.repository.entity.Chat;
import me.civka.monopoly.repository.entity.User;
import me.civka.monopoly.service.AuthService;
import me.civka.monopoly.service.exception.user.UserNotFoundException;
import org.springframework.beans.factory.annotation.Value;
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
  public static final String USER_DESTINATION_PREFIX = "/user/";

  private final AuthService authService;
  private final ChatRepository chatRepository;

  @Value("${app.chat.public-chat-reference}")
  private String publicChatReference;

  @Override
  public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

    if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
      message = interceptChatSubscription(accessor, message);
      message = interceptUserSubscription(accessor, message);
    }

    return message;
  }

  private Message<?> interceptChatSubscription(StompHeaderAccessor accessor, Message<?> message) {
    String destination = accessor.getDestination();
    if (destination == null) {
      return null;
    }

    if (destination.startsWith(CHAT_DESTINATION_PREFIX)) {
      UUID chatReference;
      try {
        chatReference = UUID.fromString(destination.substring(CHAT_DESTINATION_PREFIX.length()));
      } catch (IllegalArgumentException e) {
        return null;
      }

      if (publicChatReference.equals(chatReference.toString())) {
        return message;
      }

      Chat chat = chatRepository.findById(chatReference).orElse(null);

      if (chat == null) {
        return null;
      }

      if (!chat.isPublic()) {
        try {
          Authentication authentication =
              authService.authenticate(accessor.getFirstNativeHeader(AUTHORIZATION_HEADER));
          if (authentication == null || chat.isUserAbsent((User) authentication.getPrincipal())) {
            return null;
          }
        } catch (UserNotFoundException e) {
          return null;
        }
      }
    }

    return message;
  }

  private Message<?> interceptUserSubscription(StompHeaderAccessor accessor, Message<?> message) {
    String destination = accessor.getDestination();
    if (destination == null) {
      return null;
    }

    if (destination.startsWith(USER_DESTINATION_PREFIX)) {
      try {
        Authentication authentication =
            authService.authenticate(accessor.getFirstNativeHeader(AUTHORIZATION_HEADER));
        if (authentication == null) {
          return null;
        }

        String username = destination.split("/")[2];

        if (!authentication.getName().equals(username)) {
          return null;
        }
      } catch (UserNotFoundException e) {
        return null;
      }
    }

    return message;
  }
}
