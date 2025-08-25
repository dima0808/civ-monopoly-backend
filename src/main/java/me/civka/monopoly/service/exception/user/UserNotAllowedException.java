package me.civka.monopoly.service.exception.user;

public class UserNotAllowedException extends RuntimeException {

  public UserNotAllowedException(String message) {
    super(message);
  }
}
