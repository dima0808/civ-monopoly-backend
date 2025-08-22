package me.civka.monopoly.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.civka.monopoly.dto.message.MessageDto;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChatMessage {

  private MessageDto message;
  private MessageType type;

  public enum MessageType {
    SEND,
    DELETE
  }
}
