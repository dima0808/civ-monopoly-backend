package me.civka.monopoly.dto.property;

import static me.civka.monopoly.util.GameUtils.BOARD_SIZE;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.civka.monopoly.repository.entity.Property.UpgradeType;
import me.civka.monopoly.validation.EnumValueMatch;
import org.hibernate.validator.constraints.Range;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UpgradePropertyRequestDto {

  @Range(min = 1, max = BOARD_SIZE - 1)
  private int position;

  @EnumValueMatch(enumClass = UpgradeType.class)
  private String upgradeType;
}
