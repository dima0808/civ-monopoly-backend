package me.civka.monopoly.dto.room;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.civka.monopoly.dto.member.MemberDto;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RoomDto {

  private UUID roomReference;
  private String name;
  private Integer memberLimit;
  private List<MemberDto> members;
}
