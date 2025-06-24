package me.civka.monopoly.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.civka.monopoly.dto.room.RoomDto;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RoomMessage {

  private RoomDto room;
  private MessageType type;

  public enum MessageType {
    CREATE,
    JOIN,
    LEAVE,
    KICK,
    DELETE
  }
}
