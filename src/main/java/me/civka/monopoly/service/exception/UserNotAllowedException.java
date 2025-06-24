package me.civka.monopoly.service.exception;

public class UserNotAllowedException extends RuntimeException {

  public UserNotAllowedException(String message) {
    super(message);
  }
}
