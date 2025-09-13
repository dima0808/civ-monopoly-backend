package me.civka.monopoly.util;

import java.util.List;
import me.civka.monopoly.repository.entity.Chat;

public class ChatUtils {

  public static void sortChatsByLastMessage(List<Chat> chats, boolean haveMessages) {
    chats.sort(
        (c1, c2) -> {
          boolean c1HasMessages =
              haveMessages || (c1.getMessages() != null && !c1.getMessages().isEmpty());
          boolean c2HasMessages =
              haveMessages || (c2.getMessages() != null && !c2.getMessages().isEmpty());

          if (c1HasMessages && c2HasMessages) {
            var c1Last = c1.getMessages().getLast().getTimeStamp();
            var c2Last = c2.getMessages().getLast().getTimeStamp();
            return c2Last.compareTo(c1Last);
          }

          return Boolean.compare(!c1HasMessages, !c2HasMessages);
        });
  }

  public static void sortChatsByLastMessage(List<Chat> chats) {
    sortChatsByLastMessage(chats, false);
  }
}
