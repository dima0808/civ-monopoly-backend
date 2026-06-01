package me.civka.monopoly.dto.property;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PropertyRequirementsDto {

  private String nextUpgrade;
  private Map<String, Boolean> requirements;
}
