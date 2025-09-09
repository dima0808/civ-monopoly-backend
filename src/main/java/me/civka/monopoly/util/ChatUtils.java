package me.civka.monopoly.util;

import java.util.List;
import me.civka.monopoly.repository.entity.Chat;

public class ChatUtils {

  public static void sortChatsByLastMessage(List<Chat> chats) {
    chats.sort(
        (c1, c2) -> {
          boolean c1HasMessages = c1.getMessages() != null && !c1.getMessages().isEmpty();
          boolean c2HasMessages = c2.getMessages() != null && !c2.getMessages().isEmpty();
          return Boolean.compare(!c1HasMessages, !c2HasMessages);
        });
  }
}
