package me.civka.monopoly.service.exception.property;

import me.civka.monopoly.common.Requirement;

public class RequirementNotFulfilledException extends RuntimeException {

  private static final String MESSAGE = "Requirement '%s' is not fulfilled";

  public RequirementNotFulfilledException(Requirement requirement) {
    super(String.format(MESSAGE, requirement));
  }
}
