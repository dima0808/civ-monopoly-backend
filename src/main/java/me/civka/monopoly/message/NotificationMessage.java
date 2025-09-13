package me.civka.monopoly.message;

import static me.civka.monopoly.message.payload.MessagePayload.USER_BECAME_OWNER_MESSAGE;
import static me.civka.monopoly.message.payload.MessagePayload.USER_WAS_KICKED_MESSAGE;

import me.civka.monopoly.dto.message.MessageDto;
import me.civka.monopoly.message.payload.MessagePayload;

public record NotificationMessage(
    MessageDto message, MessagePayload messagePayload, NotificationType type) {

  public static NotificationMessage of(MessageDto message, NotificationType type) {
    return new NotificationMessage(message, null, type);
  }

  public static NotificationMessage of(NotificationType type) {
    MessagePayload messagePayload =
        switch (type) {
          case USER_WAS_KICKED -> MessagePayload.of(USER_WAS_KICKED_MESSAGE);
          case USER_BECAME_OWNER -> MessagePayload.of(USER_BECAME_OWNER_MESSAGE);
          default -> MessagePayload.of("");
        };
    return new NotificationMessage(null, messagePayload, type);
  }

  public enum NotificationType {
    PRIVATE_MESSAGE_SENT,
    USER_WAS_KICKED,
    USER_BECAME_OWNER,
  }
}
