package me.civka.monopoly.dto.user;

import java.util.UUID;
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
public class UserDto {

  private UUID reference;
  private String username;
  private String avatarUrl;
  private RoomDto room;

  private UserStatsDto stats;
}
