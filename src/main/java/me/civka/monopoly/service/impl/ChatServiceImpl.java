package me.civka.monopoly.service.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.chat.ChatDto;
import me.civka.monopoly.dto.message.MessageDto;
import me.civka.monopoly.dto.message.MessageRequestDto;
import me.civka.monopoly.repository.AuthorityRepository;
import me.civka.monopoly.repository.ChatRepository;
import me.civka.monopoly.repository.MessageRepository;
import me.civka.monopoly.repository.UserRepository;
import me.civka.monopoly.repository.entity.Authority;
import me.civka.monopoly.repository.entity.Authority.AuthorityName;
import me.civka.monopoly.repository.entity.Chat;
import me.civka.monopoly.repository.entity.Message;
import me.civka.monopoly.repository.entity.User;
import me.civka.monopoly.service.ChatService;
import me.civka.monopoly.service.exception.AuthorityNotFoundException;
import me.civka.monopoly.service.exception.ChatAlreadyExistsException;
import me.civka.monopoly.service.exception.ChatNotFoundException;
import me.civka.monopoly.service.exception.UserNotAllowedException;
import me.civka.monopoly.service.exception.UserNotFoundException;
import me.civka.monopoly.service.mapper.ChatMapper;
import me.civka.monopoly.service.mapper.MessageMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

  private final UserRepository userRepository;
  private final AuthorityRepository authorityRepository;
  private final ChatMapper chatMapper;
  private final ChatRepository chatRepository;
  private final MessageMapper messageMapper;
  private final MessageRepository messageRepository;
  private final SimpMessagingTemplate messagingTemplate;

  @Override
  public List<ChatDto> getAllChatsByUsername(String username) {
    return List.of();
  }

  @Override
  public ChatDto createPrivateChat(String receiverUsername) {
    User sender = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User receiver =
        userRepository
            .findByUsername(receiverUsername)
            .orElseThrow(() -> new UserNotFoundException(receiverUsername));

    if (chatRepository.existsByUsers(List.of(sender, receiver))) {
      throw new ChatAlreadyExistsException(sender.getUsername(), receiver.getUsername());
    }

    Chat chat = Chat.builder().users(List.of(sender, receiver)).build();

    return chatMapper.toChatDto(chatRepository.save(chat));
  }

  @Override
  public MessageDto sendMessage(UUID chatReference, MessageRequestDto messageRequestDto) {
    User sender = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Chat chat =
        chatRepository
            .findById(chatReference)
            .orElseThrow(() -> new ChatNotFoundException(chatReference));

    if (sender.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(AuthorityName.ROLE_MUTED.name()))
        && chat.getUsers().isEmpty()) {
      throw new UserNotAllowedException("Muted users cannot send messages");
    }

    MessageDto messageDto =
        messageMapper.toMessageDto(
            messageRepository.save(
                Message.builder()
                    .message(messageRequestDto.getMessage())
                    .sender(sender)
                    .chat(chat)
                    .timeStamp(OffsetDateTime.now())
                    .build()));

    messagingTemplate.convertAndSend("/topic/chats/" + chatReference, messageDto);

    return messageDto;
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
  public void deleteMessage(UUID messageReference) {
    messageRepository.deleteById(messageReference);
  }
}
