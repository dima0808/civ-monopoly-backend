package me.civka.monopoly.service.exception;

public class UserAlreadyExistsException extends RuntimeException {

  private static final String MESSAGE = "User with username '%s' already exists";

  public UserAlreadyExistsException(String username) {
    super(String.format(MESSAGE, username));
  }
}
