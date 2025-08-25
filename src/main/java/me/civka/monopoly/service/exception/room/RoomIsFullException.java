package me.civka.monopoly.service.exception.room;

public class RoomIsFullException extends RuntimeException {

  private static final String MESSAGE = "Room '%s' is full";

  public RoomIsFullException(String name) {
    super(String.format(MESSAGE, name));
  }
}
