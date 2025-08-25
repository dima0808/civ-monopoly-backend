package me.civka.monopoly.service.exception.user;

import jakarta.persistence.EntityNotFoundException;
import me.civka.monopoly.repository.entity.Authority.AuthorityName;

public class AuthorityNotFoundException extends EntityNotFoundException {

  private static final String MESSAGE = "Authority with name '%s' not found";

  public AuthorityNotFoundException(AuthorityName authority) {
    super(String.format(MESSAGE, authority));
  }
}
