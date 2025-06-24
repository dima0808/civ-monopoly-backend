package me.civka.monopoly.service.exception;

import java.util.UUID;

public class UserAlreadyInRoomException extends RuntimeException {

  private static final String MESSAGE = "User with reference '%s' is already in the room";

  public UserAlreadyInRoomException(UUID userReference) {
    super(String.format(MESSAGE, userReference));
  }
}
