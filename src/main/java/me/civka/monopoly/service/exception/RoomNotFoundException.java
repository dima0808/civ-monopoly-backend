package me.civka.monopoly.service.exception;

import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;

public class RoomNotFoundException extends EntityNotFoundException {

  private static final String MESSAGE = "Room with reference '%s' not found";

  public RoomNotFoundException(UUID roomReference) {
    super(String.format(MESSAGE, roomReference));
  }
}
