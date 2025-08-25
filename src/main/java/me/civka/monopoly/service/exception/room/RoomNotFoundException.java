package me.civka.monopoly.service.exception.room;

import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import me.civka.monopoly.repository.entity.Member;

public class RoomNotFoundException extends EntityNotFoundException {

  private static final String MESSAGE = "Room with reference '%s' not found";
  private static final String MESSAGE_MEMBER = "Member with reference '%s' not in room";

  public RoomNotFoundException(UUID roomReference) {
    super(String.format(MESSAGE, roomReference));
  }

  public RoomNotFoundException(Member member) {
    super(String.format(MESSAGE_MEMBER, member.getReference()));
  }
}
