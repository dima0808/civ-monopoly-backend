package me.civka.monopoly.service.exception.member;

import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;

public class MessageNotFoundException extends EntityNotFoundException {

  private static final String MESSAGE = "Message with reference '%s' not found";

  public MessageNotFoundException(UUID messageReference) {
    super(String.format(MESSAGE, messageReference));
  }
}
