package me.civka.monopoly.service.exception.user;

public class UserNotInRoomException extends RuntimeException {

  private static final String MESSAGE = "User '%s' is not in room";

  public UserNotInRoomException(String username) {
    super(String.format(MESSAGE, username));
  }
}
