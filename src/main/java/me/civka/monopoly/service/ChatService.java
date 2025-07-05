package me.civka.monopoly.service;

import java.util.List;
import java.util.UUID;
import me.civka.monopoly.dto.chat.ChatDto;
import me.civka.monopoly.dto.message.MessageDto;
import me.civka.monopoly.dto.message.MessageRequestDto;

public interface ChatService {

  List<ChatDto> getAllChatsByUsername(String username);

  ChatDto createPrivateChat(String receiverUsername);

  MessageDto sendMessage(UUID chatReference, MessageRequestDto messageRequestDto);

  void muteUser(String username);

  void unmuteUser(String username);

  void deleteMessage(UUID messageReference);
}
