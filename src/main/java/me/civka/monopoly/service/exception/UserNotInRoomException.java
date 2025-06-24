package me.civka.monopoly.service.exception;

import java.util.UUID;

public class UserNotInRoomException extends RuntimeException {

  public UserNotInRoomException(String roomName, UUID userReference) {
    super(String.format("User with reference '%s' is not in room '%s'", userReference, roomName));
  }
}
