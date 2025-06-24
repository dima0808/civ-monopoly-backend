package me.civka.monopoly.service.exception;

import me.civka.monopoly.repository.entity.Authority.AuthorityName;

public class AuthorityNotFoundException extends RuntimeException {

  private static final String MESSAGE = "Authority with name '%s' not found";

  public AuthorityNotFoundException(AuthorityName authority) {
    super(String.format(MESSAGE, authority));
  }
}
