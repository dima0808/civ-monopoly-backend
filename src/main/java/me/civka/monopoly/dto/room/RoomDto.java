package me.civka.monopoly.dto.room;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.civka.monopoly.dto.member.MemberDto;
import me.civka.monopoly.dto.room.ext.RoomExtraData;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RoomDto {

  private UUID reference;
  private String name;
  private Integer memberLimit;
  private Boolean hasPassword;
  private Boolean isStarted;
  private Integer turnIndex;
  private Boolean isDiceRolled;
  private Integer turn;
  private List<MemberDto> members;

  private RoomExtraData ext;
}
