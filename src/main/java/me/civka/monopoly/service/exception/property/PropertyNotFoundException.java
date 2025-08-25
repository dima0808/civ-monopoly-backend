package me.civka.monopoly.service.exception.property;

import jakarta.persistence.EntityNotFoundException;
import me.civka.monopoly.repository.entity.Member;

public class PropertyNotFoundException extends EntityNotFoundException {

  private static final String MESSAGE = "Property on position %d not found for member %s";

  public PropertyNotFoundException(int position, Member member) {
    super(String.format(MESSAGE, position, member.getUser().getUsername()));
  }
}
