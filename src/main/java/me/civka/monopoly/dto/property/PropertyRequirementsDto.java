package me.civka.monopoly.dto.property;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PropertyRequirementsDto {

  private String nextUpgrade;
  private Map<String, Boolean> requirements;

  // For branching cells (Government Plaza): requirements for each department
  // option keyed by upgrade level (LEVEL_4_1 / LEVEL_4_2 / LEVEL_4_3). Null otherwise.
  private Map<String, Map<String, Boolean>> branchRequirements;
}
