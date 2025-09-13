package me.civka.monopoly.message.payload;

public record MessagePayload(String message) {

  public static final String USER_WAS_KICKED_MESSAGE = "You have been kicked from the room";
  public static final String USER_BECAME_OWNER_MESSAGE = "You are now the owner of the room";

  public static MessagePayload of(String message) {
    return new MessagePayload(message);
  }
}
