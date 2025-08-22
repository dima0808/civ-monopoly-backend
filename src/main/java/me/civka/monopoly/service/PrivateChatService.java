package me.civka.monopoly.service;

import java.util.UUID;
import me.civka.monopoly.dto.chat.ChatDto;
import me.civka.monopoly.dto.chat.ChatListDto;

public interface PrivateChatService extends ChatService {

  ChatListDto getAllPrivateChats();

  ChatDto getPrivateChatByReference(UUID chatReference);

  ChatDto createPrivateChat(String receiverUsername);
}
