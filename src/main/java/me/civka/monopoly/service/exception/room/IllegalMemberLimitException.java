package me.civka.monopoly.service.exception.room;

public class IllegalMemberLimitException extends RuntimeException {

  private static final String MESSAGE =
      "Room with member limit %d cannot be created (2-%d allowed)";

  public IllegalMemberLimitException(int limit, int allowedLimit) {
    super(String.format(MESSAGE, limit, allowedLimit));
  }
}
