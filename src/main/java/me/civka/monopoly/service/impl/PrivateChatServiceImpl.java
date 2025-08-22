package me.civka.monopoly.service.impl;

import java.time.OffsetDateTime;
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
            .peek(
                c -> {
                  Message lastMessage =
                      messageRepository.findTopByChatOrderByTimeStampDesc(c).orElse(null);
                  c.setMessages(lastMessage == null ? List.of() : List.of(lastMessage));
                })
            .toList();
    return chatMapper.toChatListDto(chats);
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

    if (chat.isUserAbsent(sender)) {
      throw new UserNotAllowedException("User not allowed to send messages in this private chat");
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

    convertAndSendTo("/topic/chats/" + chatReference, messageDto, MessageType.SEND);

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
}
