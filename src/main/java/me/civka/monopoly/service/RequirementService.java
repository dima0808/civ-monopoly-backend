package me.civka.monopoly.service;

import me.civka.monopoly.repository.entity.Member;
import me.civka.monopoly.repository.entity.Property;

public interface RequirementService {

  default boolean isBuyAllowed(Member member) {
    return false;
  }

  default boolean isUpgradeAllowed(Property property, Member member) {
    return false;
  }
}
