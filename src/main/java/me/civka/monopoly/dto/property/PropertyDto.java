package me.civka.monopoly.dto.property;

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
public class PropertyDto {

  private UUID reference;
  private Integer position;
  private Integer mortgage;
  private List<String> upgrades;
  private List<String> bonuses;
  private MemberDto member;
}
