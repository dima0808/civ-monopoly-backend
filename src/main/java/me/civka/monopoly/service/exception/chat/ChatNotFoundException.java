package me.civka.monopoly.service.exception.chat;

import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;

public class ChatNotFoundException extends EntityNotFoundException {

  private static final String MESSAGE = "Chat with room reference '%s' not found";
  public static final String MESSAGE_USERNAMES = "Chat between users '%s' and '%s' not found";

  public ChatNotFoundException(UUID roomReference) {
    super(String.format(MESSAGE, roomReference));
  }

  public ChatNotFoundException(String username1, String username2) {
    super(String.format(MESSAGE_USERNAMES, username1, username2));
  }
}
