package me.civka.monopoly.service.impl;

import static me.civka.monopoly.util.ChatUtils.sortChatsByLastMessage;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.chat.ChatDto;
import me.civka.monopoly.dto.chat.ChatListDto;
import me.civka.monopoly.dto.message.MessageDto;
import me.civka.monopoly.dto.message.MessageRequestDto;
import me.civka.monopoly.message.ChatMessage;
import me.civka.monopoly.message.ChatMessage.MessageType;
import me.civka.monopoly.message.NotificationMessage;
import me.civka.monopoly.message.NotificationMessage.NotificationType;
import me.civka.monopoly.repository.ChatRepository;
import me.civka.monopoly.repository.MessageRepository;
import me.civka.monopoly.repository.UserRepository;
import me.civka.monopoly.repository.entity.Chat;
import me.civka.monopoly.repository.entity.Message;
import me.civka.monopoly.repository.entity.User;
import me.civka.monopoly.service.PrivateChatService;
import me.civka.monopoly.service.exception.chat.ChatAlreadyExistsException;
import me.civka.monopoly.service.exception.chat.ChatNotFoundException;
import me.civka.monopoly.service.exception.user.UserNotAllowedException;
import me.civka.monopoly.service.exception.user.UserNotFoundException;
import me.civka.monopoly.service.mapper.ChatMapper;
import me.civka.monopoly.service.mapper.MessageMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrivateChatServiceImpl implements PrivateChatService {

  private final UserRepository userRepository;
  private final ChatMapper chatMapper;
  private final ChatRepository chatRepository;
  private final MessageMapper messageMapper;
  private final MessageRepository messageRepository;
  private final SimpMessagingTemplate messagingTemplate;

  @Override
  public ChatListDto getAllPrivateChats() {
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    List<Chat> chats =
        chatRepository.findAllByUsersContaining(user).stream()
            .peek(this::assignLastMessageToChat)
            .collect(Collectors.toCollection(ArrayList::new));

    sortChatsByLastMessage(chats, true);

    return chatMapper.toChatListDto(chats);
  }

  @Override
  public ChatListDto getAllPrivateChats(String username) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    if (username == null || username.isBlank()) {
      return getAllPrivateChats();
    }

    List<Chat> chats = chatRepository.findAllByUsersContaining(user);
    List<User> matchedUsers =
        userRepository.findByUsernameContainingIgnoreCase(username).stream()
            .filter(u -> !u.equalsById(user))
            .toList();

    List<Chat> resultChats = new ArrayList<>();

    for (User matchedUser : matchedUsers) {
      Chat chat =
          chats.stream()
              .filter(c -> c.getUsers().stream().anyMatch((u) -> u.equalsById(matchedUser)))
              .findFirst()
              .orElse(null);

      if (chat != null) {
        assignLastMessageToChat(chat);
        resultChats.add(chat);
      } else {
        Chat emptyChat =
            Chat.builder().users(List.of(user, matchedUser)).messages(List.of()).build();
        resultChats.add(emptyChat);
      }
    }

    sortChatsByLastMessage(resultChats);

    return chatMapper.toChatListDto(resultChats);
  }

  @Override
  public ChatDto getPrivateChatByReference(UUID chatReference) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Chat chat =
        chatRepository
            .findById(chatReference)
            .orElseThrow(() -> new ChatNotFoundException(chatReference));

    if (chat.isUserAbsent(user)) {
      throw new UserNotAllowedException("User not allowed to get messages in this private chat");
    }

    return chatMapper.toChatDto(chat);
  }

  @Override
  public ChatDto createPrivateChat(String receiverUsername, MessageRequestDto messageRequestDto) {
    User sender = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User receiver =
        userRepository
            .findByUsername(receiverUsername)
            .orElseThrow(() -> new UserNotFoundException(receiverUsername));

    if (chatRepository.existsByUsersReference(sender.getReference(), receiver.getReference())) {
      throw new ChatAlreadyExistsException(sender.getUsername(), receiver.getUsername());
    }

    Chat chat = chatRepository.save(Chat.builder().users(List.of(sender, receiver)).build());

    sendMessage(chat.getReference(), messageRequestDto);

    return chatMapper.toChatDto(chat);
  }

  @Override
  public MessageDto sendMessage(UUID chatReference, MessageRequestDto messageRequestDto) {
    User sender = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Chat chat =
        chatRepository
            .findById(chatReference)
            .orElseThrow(() -> new ChatNotFoundException(chatReference));

    if (chat.isUserAbsent(sender)) {
      throw new UserNotAllowedException("User not allowed to send messages in this private chat");
    }

    User receiver =
        chat.getUsers().stream()
            .filter((u) -> !u.equalsById(sender))
            .findFirst()
            .orElseThrow(() -> new UserNotFoundException("There is no receiver in this chat"));

    Message message =
        messageRepository.save(
            Message.builder()
                .message(messageRequestDto.getMessage())
                .sender(sender)
                .chat(chat)
                .timeStamp(OffsetDateTime.now())
                .build());
    MessageDto messageDto = messageMapper.toMessageDto(message);

    chat.getMessages().add(message); // for display purposes
    ChatDto chatDto = chatMapper.toChatDto(chat);

    broadcastChatMessage(chatReference, messageDto, MessageType.SEND);
    updateUserContacts(sender.getUsername(), chatDto, MessageType.SEND);
    updateUserContacts(receiver.getUsername(), chatDto, MessageType.SEND);
    notifyUser(receiver.getUsername(), messageDto, NotificationType.PRIVATE_MESSAGE_SENT);

    return messageDto;
  }

  @Override
  public void deleteMessage(UUID chatReference, UUID messageReference) {
    User sender = (User) SecurityContextHolder.getContext().getAuthentication();
    Chat chat =
        chatRepository
            .findById(chatReference)
            .orElseThrow(() -> new ChatNotFoundException(chatReference));

    if (chat.isUserAbsent(sender)) {
      throw new UserNotAllowedException("User not allowed to delete messages in this chat");
    }

    User receiver =
        chat.getUsers().stream()
            .filter((u) -> !u.equalsById(sender))
            .findFirst()
            .orElseThrow(() -> new UserNotFoundException("There is no receiver in this chat"));

    messageRepository
        .findByChatReferenceAndReference(chatReference, messageReference)
        .ifPresent(
            (message) -> {
              messageRepository.deleteById(messageReference);
              MessageDto messageDto = messageMapper.toMessageDto(message);

              boolean isLast =
                  !chat.getMessages().isEmpty()
                      && chat.getMessages().getLast().getReference().equals(messageReference);

              if (isLast) {
                chat.getMessages().removeLast(); // for display purposes
                ChatDto chatDto = chatMapper.toChatDto(chat);
                updateUserContacts(sender.getUsername(), chatDto, MessageType.DELETE);
                updateUserContacts(receiver.getUsername(), chatDto, MessageType.DELETE);
              }
              broadcastChatMessage(chatReference, messageDto, MessageType.DELETE);
            });
  }

  private void broadcastChatMessage(UUID chatReference, MessageDto messageDto, MessageType type) {
    messagingTemplate.convertAndSend(
        "/topic/chats/" + chatReference, ChatMessage.of(messageDto, type));
  }

  private void updateUserContacts(String username, ChatDto chatDto, MessageType type) {
    messagingTemplate.convertAndSendToUser(
        username, "/chats/contacts", ChatMessage.of(chatDto, type));
  }

  private void notifyUser(String username, MessageDto messageDto, NotificationType type) {
    messagingTemplate.convertAndSendToUser(
        username, "/notifications", NotificationMessage.of(messageDto, type));
  }

  private void assignLastMessageToChat(Chat chat) {
    Message lastMessage = messageRepository.findTopByChatOrderByTimeStampDesc(chat).orElse(null);
    chat.setMessages(lastMessage == null ? List.of() : List.of(lastMessage));
  }
}
