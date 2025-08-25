package me.civka.monopoly.service.exception.chat;

public class ChatAlreadyExistsException extends RuntimeException {

  private static final String MESSAGE = "Chat with users '%s' and '%s' already exists";

  public ChatAlreadyExistsException(String u1, String u2) {
    super(String.format(MESSAGE, u1, u2));
  }
}
