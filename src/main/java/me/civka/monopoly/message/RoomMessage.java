package me.civka.monopoly.message;

import me.civka.monopoly.dto.room.RoomDto;

public record RoomMessage(RoomDto room, MessageType type) {

  public static RoomMessage of(RoomDto room, MessageType type) {
    return new RoomMessage(room, type);
  }

  public enum MessageType {
    CREATE,
    JOIN,
    LEAVE,
    KICK,
    DELETE
  }
}
