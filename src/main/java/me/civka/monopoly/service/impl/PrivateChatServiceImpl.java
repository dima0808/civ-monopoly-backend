package me.civka.monopoly.service.impl;

import static me.civka.monopoly.util.ChatUtils.sortChatsByLastMessage;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.chat.ChatDto;
import me.civka.monopoly.dto.chat.ChatListDto;
import me.civka.monopoly.dto.message.MessageDto;
import me.civka.monopoly.dto.message.MessageRequestDto;
import me.civka.monopoly.message.ChatMessage;
import me.civka.monopoly.message.ChatMessage.MessageType;
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
            .toList();
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

    convertAndSendTo("/topic/chats/" + chatReference, messageDto, MessageType.SEND);
    convertAndSendToUser(sender.getUsername(), "/chats/contacts", chatDto, MessageType.SEND);
    convertAndSendToUser(receiver.getUsername(), "/chats/contacts", chatDto, MessageType.SEND);

    return messageDto;
  }

  @Override
  public void deleteMessage(UUID chatReference, UUID messageReference) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication();

    if (!chatRepository.existsByUsersContainsAndReference(user, chatReference)) {
      throw new UserNotAllowedException("User not allowed to delete messages in this chat");
    }

    messageRepository
        .findByChatReferenceAndReference(chatReference, messageReference)
        .ifPresent(
            (message) -> {
              messageRepository.deleteById(messageReference);
              convertAndSendTo(
                  "/topic/chats/" + chatReference,
                  messageMapper.toMessageDto(Message.builder().reference(messageReference).build()),
                  MessageType.DELETE);
            });
  }

  private void convertAndSendTo(String destination, MessageDto messageDto, MessageType type) {
    messagingTemplate.convertAndSend(
        destination, ChatMessage.builder().message(messageDto).type(type).build());
  }

  private void convertAndSendToUser(
      String username, String destination, ChatDto chatDto, MessageType type) {
    messagingTemplate.convertAndSendToUser(
        username, destination, ChatMessage.builder().chat(chatDto).type(type).build());
  }

  private void assignLastMessageToChat(Chat chat) {
    Message lastMessage = messageRepository.findTopByChatOrderByTimeStampDesc(chat).orElse(null);
    chat.setMessages(lastMessage == null ? List.of() : List.of(lastMessage));
  }
}
