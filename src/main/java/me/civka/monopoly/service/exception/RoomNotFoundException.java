package me.civka.monopoly.service.exception;

import java.util.UUID;

public class RoomNotFoundException extends RuntimeException {

  private static final String MESSAGE = "Room with reference '%s' not found";

  public RoomNotFoundException(UUID roomReference) {
    super(String.format(MESSAGE, roomReference));
  }
}
