package me.civka.monopoly.message;

import me.civka.monopoly.dto.chat.ChatDto;
import me.civka.monopoly.dto.message.MessageDto;

public record ChatMessage(MessageDto message, ChatDto chat, MessageType type) {

  public static ChatMessage of(MessageDto message, MessageType type) {
    return new ChatMessage(message, null, type);
  }

  public static ChatMessage of(ChatDto chat, MessageType type) {
    return new ChatMessage(null, chat, type);
  }

  public enum MessageType {
    SEND,
    DELETE
  }
}
