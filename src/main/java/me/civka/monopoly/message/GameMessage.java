package me.civka.monopoly.message;

import me.civka.monopoly.dto.room.RoomDto;

public record GameMessage(RoomDto room, MessageType type) {

  public static GameMessage of(RoomDto room, MessageType type) {
    return new GameMessage(room, type);
  }

  public enum MessageType {
    START,
    END_TURN,
    FORCE_END_TURN,
    ROLL_DICE,
    FORCE_ROLL_DICE,
    NEW_TURN,
    TELEPORT,
    CHEAT,
  }
}
