package me.civka.monopoly.service.exception.room;

public class InvalidRoomPasswordException extends RuntimeException {

  private static final String MESSAGE = "Wrong password for room '%s'";

  public InvalidRoomPasswordException(String name) {
    super(String.format(MESSAGE, name));
  }
}
