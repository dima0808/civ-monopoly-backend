package me.civka.monopoly.common;

import me.civka.monopoly.repository.entity.Member;
import me.civka.monopoly.repository.entity.Property;
import me.civka.monopoly.service.RequirementService;

public enum Requirement implements RequirementService {
  MAKE_ONE_ROUND {
    @Override
    public boolean isUpgradeAllowed(Property property, Member member) {
      return member.getRoundsMade() - property.getRoundOfLastChange() >= 1;
    }
  },
}
