package me.civka.monopoly.dto.chat;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.civka.monopoly.dto.message.MessageDto;
import me.civka.monopoly.dto.user.UserDto;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChatDto {

  private UUID reference;
  private List<UserDto> users;
  private List<MessageDto> messages;
}
