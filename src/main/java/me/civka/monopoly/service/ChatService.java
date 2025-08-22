package me.civka.monopoly.service;

import java.util.UUID;
import me.civka.monopoly.dto.message.MessageDto;
import me.civka.monopoly.dto.message.MessageRequestDto;

public interface ChatService {

  MessageDto sendMessage(UUID chatReference, MessageRequestDto messageRequestDto);

  void deleteMessage(UUID chatReference, UUID messageReference);
}
