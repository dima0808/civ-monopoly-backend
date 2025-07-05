package me.civka.monopoly.service.exception;

import jakarta.persistence.EntityNotFoundException;

public class UserNotFoundException extends EntityNotFoundException {

  private static final String MESSAGE = "User with username '%s' not found";

  public UserNotFoundException(String username) {
    super(String.format(MESSAGE, username));
  }
}
