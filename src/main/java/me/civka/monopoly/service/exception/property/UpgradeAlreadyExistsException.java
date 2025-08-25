package me.civka.monopoly.service.exception.property;

import me.civka.monopoly.repository.entity.Property.UpgradeType;

public class UpgradeAlreadyExistsException extends RuntimeException {

  private static final String MESSAGE = "Upgrade '%s' already exists on this property";

  public UpgradeAlreadyExistsException(UpgradeType upgradeType) {
    super(String.format(MESSAGE, upgradeType));
  }
}
