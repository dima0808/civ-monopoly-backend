package me.civka.monopoly.service.exception.member;

import java.util.UUID;

public class MemberNotFoundException extends RuntimeException {

  private static final String MESSAGE = "Member with reference '%s' not found";

  public MemberNotFoundException(UUID memberReference) {
    super(String.format(MESSAGE, memberReference));
  }
}
