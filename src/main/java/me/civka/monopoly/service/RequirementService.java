package me.civka.monopoly.service;

import java.util.List;
import me.civka.monopoly.repository.entity.Member;
import me.civka.monopoly.repository.entity.Property;
import me.civka.monopoly.repository.entity.Room;

public interface RequirementService {

  default boolean isBuyAllowed(Member member, Room room, List<Property> ownedProperties) {
    return false;
  }

  default boolean isUpgradeAllowed(
      Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
    return false;
  }
}
