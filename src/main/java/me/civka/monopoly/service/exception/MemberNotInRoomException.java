package me.civka.monopoly.service.exception;

import java.util.UUID;

public class MemberNotInRoomException extends RuntimeException {

  private static final String MESSAGE = "Member with reference '%s' is not in room '%s'";

  public MemberNotInRoomException(String roomName, UUID memberReference) {
    super(String.format(MESSAGE, memberReference, roomName));
  }
}
