package me.civka.monopoly.dto.property;

import static me.civka.monopoly.util.GameUtils.BOARD_SIZE;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PropertyRequestDto {

  @Range(min = 1, max = BOARD_SIZE - 1)
  private int position;
}
