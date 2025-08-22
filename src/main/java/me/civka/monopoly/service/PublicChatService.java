package me.civka.monopoly.service;

import java.util.UUID;
import me.civka.monopoly.dto.chat.ChatDto;

public interface PublicChatService extends ChatService {

  ChatDto getPublicChatByReference(UUID chatReference);

  void muteUser(String username);

  void unmuteUser(String username);
}
