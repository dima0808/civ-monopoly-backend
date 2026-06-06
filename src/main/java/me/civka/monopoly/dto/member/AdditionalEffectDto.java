package me.civka.monopoly.dto.member;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class AdditionalEffectDto {

  private String type;
  private Integer turnsLeft;
  private Integer goldPerTurn;
}
