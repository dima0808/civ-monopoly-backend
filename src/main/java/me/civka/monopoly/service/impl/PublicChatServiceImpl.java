package me.civka.monopoly.service.impl;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.chat.ChatDto;
import me.civka.monopoly.dto.message.MessageDto;
import me.civka.monopoly.dto.message.MessageRequestDto;
import me.civka.monopoly.message.ChatMessage;
import me.civka.monopoly.message.ChatMessage.MessageType;
import me.civka.monopoly.repository.AuthorityRepository;
import me.civka.monopoly.repository.ChatRepository;
import me.civka.monopoly.repository.MessageRepository;
import me.civka.monopoly.repository.UserRepository;
import me.civka.monopoly.repository.entity.Authority;
import me.civka.monopoly.repository.entity.Authority.AuthorityName;
import me.civka.monopoly.repository.entity.Chat;
import me.civka.monopoly.repository.entity.Message;
import me.civka.monopoly.repository.entity.User;
import me.civka.monopoly.service.PublicChatService;
import me.civka.monopoly.service.exception.chat.ChatNotFoundException;
import me.civka.monopoly.service.exception.member.MessageNotFoundException;
import me.civka.monopoly.service.exception.user.AuthorityNotFoundException;
import me.civka.monopoly.service.exception.user.UserNotAllowedException;
import me.civka.monopoly.service.exception.user.UserNotFoundException;
import me.civka.monopoly.service.mapper.ChatMapper;
import me.civka.monopoly.service.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublicChatServiceImpl implements PublicChatService {

  private final UserRepository userRepository;
  private final AuthorityRepository authorityRepository;
  private final ChatMapper chatMapper;
  private final ChatRepository chatRepository;
  private final MessageMapper messageMapper;
  private final MessageRepository messageRepository;
  private final SimpMessagingTemplate messagingTemplate;

  @Value("${app.chat.public-chat-reference}")
  private String publicChatReference;

  @Override
  public ChatDto getPublicChatByReference(UUID chatReference) {
    Chat chat =
        chatRepository
            .findById(chatReference)
            .orElseThrow(() -> new ChatNotFoundException(chatReference));

    if (!chatReference.equals(UUID.fromString(publicChatReference)) && !chat.isPublic()) {
      throw new UserNotAllowedException("It is not a public chat");
    }

    return chatMapper.toChatDto(chat);
  }

  @Override
  public void muteUser(String username) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));
    Authority mutedAuthority =
        authorityRepository
            .findByAuthority(AuthorityName.ROLE_MUTED)
            .orElseThrow(() -> new AuthorityNotFoundException(AuthorityName.ROLE_MUTED));
    user.getAuthorities().add(mutedAuthority);
    userRepository.save(user);
  }

  @Override
  public void unmuteUser(String username) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));
    user.getAuthorities()
        .removeIf(authority -> authority.getAuthority().equals(AuthorityName.ROLE_MUTED.name()));
    userRepository.save(user);
  }

  @Override
  public MessageDto sendMessage(UUID chatReference, MessageRequestDto messageRequestDto) {
    User sender = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Chat chat =
        chatRepository
            .findById(chatReference)
            .orElseThrow(() -> new ChatNotFoundException(chatReference));

    if (sender.hasAuthority(AuthorityName.ROLE_MUTED)) {
      throw new UserNotAllowedException("Muted users cannot send messages");
    }

    Message message =
        messageRepository.save(
            Message.builder()
                .message(messageRequestDto.getMessage())
                .sender(sender)
                .chat(chat)
                .timeStamp(OffsetDateTime.now())
                .build());

    MessageDto messageDto = messageMapper.toMessageDto(message);

    broadcastChatMessage(chatReference, messageDto, MessageType.SEND);

    return messageDto;
  }

  @Override
  public void deleteMessage(UUID chatReference, UUID messageReference) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication();
    Message message =
        messageRepository
            .findByChatReferenceAndReference(chatReference, messageReference)
            .orElseThrow(() -> new MessageNotFoundException(messageReference));

    if (!message.getSender().equalsById(user) && !user.hasAuthority(AuthorityName.ROLE_ADMIN)) {
      throw new UserNotAllowedException("User not allowed to delete other's messages");
    }

    messageRepository.delete(message);

    broadcastChatMessage(chatReference, messageMapper.toMessageDto(message), MessageType.DELETE);
  }

  private void broadcastChatMessage(UUID chatReference, MessageDto messageDto, MessageType type) {
    messagingTemplate.convertAndSend(
        "/topic/chats/" + chatReference, ChatMessage.of(messageDto, type));
  }
}
