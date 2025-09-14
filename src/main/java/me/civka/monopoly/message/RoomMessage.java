package me.civka.monopoly.message;

import me.civka.monopoly.dto.member.MemberDto;
import me.civka.monopoly.dto.room.RoomDto;

public record RoomMessage(RoomDto room, MemberDto member, MessageType type) {

  public static RoomMessage of(RoomDto room, MessageType type) {
    return new RoomMessage(room, null, type);
  }

  public static RoomMessage of(MemberDto member, MessageType type) {
    return new RoomMessage(null, member, type);
  }

  public enum MessageType {
    CREATE,
    JOIN,
    LEAVE,
    KICK,
    DELETE,
    CHANGE_CIVILIZATION,
    CHANGE_COLOR
  }
}
