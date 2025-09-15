package me.civka.monopoly.service;

import java.util.UUID;
import me.civka.monopoly.dto.chat.ChatDto;
import me.civka.monopoly.dto.chat.ChatListDto;
import me.civka.monopoly.dto.message.MessageRequestDto;

public interface PrivateChatService extends ChatService {

  ChatListDto getAllPrivateChats();

  ChatListDto getAllPrivateChats(String username);

  ChatDto getPrivateChatByReference(UUID chatReference);

  ChatDto getPrivateChatByUsernames(String username1, String username2);

  ChatDto createPrivateChat(String receiverUsername, MessageRequestDto messageRequestDto);
}
