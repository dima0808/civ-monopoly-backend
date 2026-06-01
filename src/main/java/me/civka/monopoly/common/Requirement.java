package me.civka.monopoly.common;

import static me.civka.monopoly.util.PropertyUtils.CAMPUS_PROPERTY_NAME;
import static me.civka.monopoly.util.PropertyUtils.COMMERCIAL_HUB_PROPERTY_NAME;
import static me.civka.monopoly.util.PropertyUtils.DEER_PROPERTY_NAME;
import static me.civka.monopoly.util.PropertyUtils.ENCAMPMENT_PROPERTY_NAME;
import static me.civka.monopoly.util.PropertyUtils.ENTERTAINMENT_COMPLEX_PROPERTY_NAME;
import static me.civka.monopoly.util.PropertyUtils.FURS_PROPERTY_NAME;
import static me.civka.monopoly.util.PropertyUtils.GOVERNMENT_PLAZA_PROPERTY_NAME;
import static me.civka.monopoly.util.PropertyUtils.HARBOR_PROPERTY_NAME;
import static me.civka.monopoly.util.PropertyUtils.INDUSTRIAL_ZONE_PROPERTY_NAME;
import static me.civka.monopoly.util.PropertyUtils.SPACEPORT_PROPERTY_NAME;
import static me.civka.monopoly.util.PropertyUtils.calculateGpt;
import static me.civka.monopoly.util.PropertyUtils.getPositionByName;
import static me.civka.monopoly.util.PropertyUtils.getPositionByType;

import java.util.List;
import me.civka.monopoly.config.ConfigurationHolder;
import me.civka.monopoly.repository.entity.Member;
import me.civka.monopoly.repository.entity.Property;
import me.civka.monopoly.repository.entity.Property.UpgradeType;
import me.civka.monopoly.repository.entity.Room;
import me.civka.monopoly.service.RequirementService;

public enum Requirement implements RequirementService {
  MAKE_ONE_ROUND {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return member.getRoundsMade() - propertyToUpgrade.getRoundOfLastChange() >= 1;
    }
  },

  MAKE_TWO_ROUNDS {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return member.getRoundsMade() - propertyToUpgrade.getRoundOfLastChange() >= 2;
    }
  },

  MAKE_THREE_ROUNDS {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return member.getRoundsMade() - propertyToUpgrade.getRoundOfLastChange() >= 3;
    }
  },

  MAKE_ONE_TURN {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return room.getTurn() - propertyToUpgrade.getTurnOfLastChange() >= 1;
    }
  },

  MAKE_TWO_TURNS {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return room.getTurn() - propertyToUpgrade.getTurnOfLastChange() >= 2;
    }
  },

  MAKE_THREE_TURNS {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return room.getTurn() - propertyToUpgrade.getTurnOfLastChange() >= 3;
    }
  },

  MAKE_FOUR_TURNS {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return room.getTurn() - propertyToUpgrade.getTurnOfLastChange() >= 4;
    }
  },

  OWN_ENCAMPMENT {
    @Override
    public boolean isBuyAllowed(Member member, Room room, List<Property> ownedProperties) {
      List<Integer> encampmentPos = getPositionByName(ENCAMPMENT_PROPERTY_NAME);
      return ownedProperties.stream().anyMatch(p -> encampmentPos.contains(p.getPosition()));
    }
  },

  OWN_HARBOR {
    @Override
    public boolean isBuyAllowed(Member member, Room room, List<Property> ownedProperties) {
      List<Integer> harborPos = getPositionByName(HARBOR_PROPERTY_NAME);
      return ownedProperties.stream().anyMatch(p -> harborPos.contains(p.getPosition()));
    }
  },

  OWN_SPACEPORT_OR_LAB {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      List<Integer> spaceportPos = getPositionByName(SPACEPORT_PROPERTY_NAME);
      List<Integer> campusPos = getPositionByName(CAMPUS_PROPERTY_NAME);
      return ownedProperties.stream()
          .anyMatch(
              p ->
                  spaceportPos.contains(p.getPosition())
                      || (campusPos.contains(p.getPosition())
                          && p.getUpgrades().contains(UpgradeType.LEVEL_4)));
    }
  },

  OWN_CAMP {
    @Override
    public boolean isBuyAllowed(Member member, Room room, List<Property> ownedProperties) {
      List<Integer> deerPos = getPositionByName(DEER_PROPERTY_NAME);
      List<Integer> fursPos = getPositionByName(FURS_PROPERTY_NAME);
      return ownedProperties.stream()
          .anyMatch(
              p ->
                  (deerPos.contains(p.getPosition()) || fursPos.contains(p.getPosition()))
                      && p.getUpgrades().contains(UpgradeType.LEVEL_2));
    }
  },

  OWN_LIBRARY {
    @Override
    public boolean isBuyAllowed(Member member, Room room, List<Property> ownedProperties) {
      List<Integer> campusPos = getPositionByName(CAMPUS_PROPERTY_NAME);
      return ownedProperties.stream()
          .anyMatch(
              p ->
                  campusPos.contains(p.getPosition())
                      && p.getUpgrades().contains(UpgradeType.LEVEL_2));
    }
  },

  OWN_UNIVERSITY {
    @Override
    public boolean isBuyAllowed(Member member, Room room, List<Property> ownedProperties) {
      List<Integer> campusPos = getPositionByName(CAMPUS_PROPERTY_NAME);
      return ownedProperties.stream()
          .anyMatch(
              p ->
                  campusPos.contains(p.getPosition())
                      && p.getUpgrades().contains(UpgradeType.LEVEL_3));
    }
  },

  OWN_ARENA {
    @Override
    public boolean isBuyAllowed(Member member, Room room, List<Property> ownedProperties) {
      List<Integer> entertainmentPos = getPositionByName(ENTERTAINMENT_COMPLEX_PROPERTY_NAME);
      return ownedProperties.stream()
          .anyMatch(
              p ->
                  entertainmentPos.contains(p.getPosition())
                      && p.getUpgrades().contains(UpgradeType.LEVEL_2));
    }
  },

  OWN_STADIUM {
    @Override
    public boolean isBuyAllowed(Member member, Room room, List<Property> ownedProperties) {
      List<Integer> entertainmentPos = getPositionByName(ENTERTAINMENT_COMPLEX_PROPERTY_NAME);
      return ownedProperties.stream()
          .anyMatch(
              p ->
                  entertainmentPos.contains(p.getPosition())
                      && p.getUpgrades().contains(UpgradeType.LEVEL_4));
    }
  },

  OWN_FACTORY {
    @Override
    public boolean isBuyAllowed(Member member, Room room, List<Property> ownedProperties) {
      List<Integer> industrialPos = getPositionByName(INDUSTRIAL_ZONE_PROPERTY_NAME);
      return ownedProperties.stream()
          .anyMatch(
              p ->
                  industrialPos.contains(p.getPosition())
                      && p.getUpgrades().contains(UpgradeType.LEVEL_3));
    }
  },

  OWN_STOCK_EXCHANGE {
    @Override
    public boolean isBuyAllowed(Member member, Room room, List<Property> ownedProperties) {
      List<Integer> commercialPos = getPositionByName(COMMERCIAL_HUB_PROPERTY_NAME);
      return ownedProperties.stream()
          .anyMatch(
              p ->
                  commercialPos.contains(p.getPosition())
                      && p.getUpgrades().contains(UpgradeType.LEVEL_4));
    }
  },

  OWN_TWO_WONDERS {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      List<Integer> wonderPos = getPositionByType(PropertyType.WONDER);
      return ownedProperties.stream().filter(p -> wonderPos.contains(p.getPosition())).count() >= 2;
    }
  },

  OWN_TWO_RESOURCES {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      List<Integer> resourcePos = getPositionByType(PropertyType.RESOURCE);
      return ownedProperties.stream().filter(p -> resourcePos.contains(p.getPosition())).count()
          >= 2;
    }
  },

  OWN_GOVERNMENT_PLAZA {
    @Override
    public boolean isBuyAllowed(Member member, Room room, List<Property> ownedProperties) {
      List<Integer> govPlazaPos = getPositionByName(GOVERNMENT_PLAZA_PROPERTY_NAME);
      return ownedProperties.stream().anyMatch(p -> govPlazaPos.contains(p.getPosition()));
    }
  },

  OWN_NO_GOVERNMENT_PLAZA {
    @Override
    public boolean isBuyAllowed(Member member, Room room, List<Property> ownedProperties) {
      List<Integer> govPlazaPos = getPositionByName(GOVERNMENT_PLAZA_PROPERTY_NAME);
      return ownedProperties.stream().noneMatch(p -> govPlazaPos.contains(p.getPosition()));
    }
  },

  HAVE_LOW_GOLD {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return member.getGold()
          >= ConfigurationHolder.requirementsConfiguration()
              .requirements()
              .get(HAVE_LOW_GOLD)
              .params()
              .getFirst();
    }
  },

  HAVE_MEDIUM_GOLD {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return member.getGold()
          >= ConfigurationHolder.requirementsConfiguration()
              .requirements()
              .get(HAVE_MEDIUM_GOLD)
              .params()
              .getFirst();
    }
  },

  HAVE_HIGH_GOLD {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return member.getGold()
          >= ConfigurationHolder.requirementsConfiguration()
              .requirements()
              .get(HAVE_HIGH_GOLD)
              .params()
              .getFirst();
    }
  },

  HAVE_LOW_GOLD_PER_TURN {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return calculateGpt(ownedProperties)
          >= ConfigurationHolder.requirementsConfiguration()
              .requirements()
              .get(HAVE_LOW_GOLD_PER_TURN)
              .params()
              .getFirst();
    }
  },

  HAVE_MEDIUM_GOLD_PER_TURN {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return calculateGpt(ownedProperties)
          >= ConfigurationHolder.requirementsConfiguration()
              .requirements()
              .get(HAVE_MEDIUM_GOLD_PER_TURN)
              .params()
              .getFirst();
    }
  },

  HAVE_HIGH_GOLD_PER_TURN {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return calculateGpt(ownedProperties)
          >= ConfigurationHolder.requirementsConfiguration()
              .requirements()
              .get(HAVE_HIGH_GOLD_PER_TURN)
              .params()
              .getFirst();
    }
  },

  HAVE_LOW_STRENGTH {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return member.getStrength()
          >= ConfigurationHolder.requirementsConfiguration()
              .requirements()
              .get(HAVE_LOW_STRENGTH)
              .params()
              .getFirst();
    }
  },

  HAVE_MEDIUM_STRENGTH {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return member.getStrength()
          >= ConfigurationHolder.requirementsConfiguration()
              .requirements()
              .get(HAVE_MEDIUM_STRENGTH)
              .params()
              .getFirst();
    }
  },

  HAVE_HIGH_STRENGTH {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return member.getStrength()
          >= ConfigurationHolder.requirementsConfiguration()
              .requirements()
              .get(HAVE_HIGH_STRENGTH)
              .params()
              .getFirst();
    }
  },

  HAVE_MEDIUM_TOURISM {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return member.getTourism()
          >= ConfigurationHolder.requirementsConfiguration()
              .requirements()
              .get(HAVE_MEDIUM_TOURISM)
              .params()
              .getFirst();
    }
  },

  HAVE_HIGH_TOURISM {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return member.getTourism()
          >= ConfigurationHolder.requirementsConfiguration()
              .requirements()
              .get(HAVE_HIGH_TOURISM)
              .params()
              .getFirst();
    }
  },

  HAVE_RESEARCH_GRANTS {
    // TODO: implement
  },

  ON_CLASSICAL_ERA {
    @Override
    public boolean isBuyAllowed(Member member, Room room, List<Property> ownedProperties) {
      return room.getTurn() >= ConfigurationHolder.gameConfiguration().eras().get(Era.CLASSICAL);
    }

    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return room.getTurn() >= ConfigurationHolder.gameConfiguration().eras().get(Era.CLASSICAL);
    }
  },

  ON_MEDIEVAL_ERA {
    @Override
    public boolean isBuyAllowed(Member member, Room room, List<Property> ownedProperties) {
      return room.getTurn() >= ConfigurationHolder.gameConfiguration().eras().get(Era.MEDIEVAL);
    }

    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return room.getTurn() >= ConfigurationHolder.gameConfiguration().eras().get(Era.MEDIEVAL);
    }
  },

  ON_RENAISSANCE_ERA {
    @Override
    public boolean isBuyAllowed(Member member, Room room, List<Property> ownedProperties) {
      return room.getTurn() >= ConfigurationHolder.gameConfiguration().eras().get(Era.RENAISSANCE);
    }

    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return room.getTurn() >= ConfigurationHolder.gameConfiguration().eras().get(Era.RENAISSANCE);
    }
  },

  ON_INDUSTRIAL_ERA {
    @Override
    public boolean isBuyAllowed(Member member, Room room, List<Property> ownedProperties) {
      return room.getTurn() >= ConfigurationHolder.gameConfiguration().eras().get(Era.RENAISSANCE);
    }

    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return room.getTurn() >= ConfigurationHolder.gameConfiguration().eras().get(Era.INDUSTRIAL);
    }
  },

  ON_MODERN_ERA {
    @Override
    public boolean isBuyAllowed(Member member, Room room, List<Property> ownedProperties) {
      return room.getTurn() >= ConfigurationHolder.gameConfiguration().eras().get(Era.RENAISSANCE);
    }

    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return room.getTurn() >= ConfigurationHolder.gameConfiguration().eras().get(Era.MODERN);
    }
  },

  ON_ATOMIC_ERA {
    @Override
    public boolean isBuyAllowed(Member member, Room room, List<Property> ownedProperties) {
      return room.getTurn() >= ConfigurationHolder.gameConfiguration().eras().get(Era.RENAISSANCE);
    }

    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return room.getTurn() >= ConfigurationHolder.gameConfiguration().eras().get(Era.ATOMIC);
    }
  },

  TALL_EMPIRE {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return ownedProperties.stream()
              .flatMap(p -> p.getUpgrades().stream())
              .filter(u -> u != UpgradeType.LEVEL_1)
              .count()
          >= ConfigurationHolder.requirementsConfiguration()
              .requirements()
              .get(TALL_EMPIRE)
              .params()
              .getFirst();
    }
  },

  SUPER_TALL_EMPIRE {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return ownedProperties.stream()
              .flatMap(p -> p.getUpgrades().stream())
              .filter(u -> u != UpgradeType.LEVEL_1)
              .count()
          >= ConfigurationHolder.requirementsConfiguration()
              .requirements()
              .get(SUPER_TALL_EMPIRE)
              .params()
              .getFirst();
    }
  },

  WIDE_EMPIRE {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return ownedProperties.size()
          >= ConfigurationHolder.requirementsConfiguration()
              .requirements()
              .get(WIDE_EMPIRE)
              .params()
              .getFirst();
    }
  },

  SUPER_WIDE_EMPIRE {
    @Override
    public boolean isUpgradeAllowed(
        Property propertyToUpgrade, Member member, Room room, List<Property> ownedProperties) {
      return ownedProperties.size()
          >= ConfigurationHolder.requirementsConfiguration()
              .requirements()
              .get(SUPER_WIDE_EMPIRE)
              .params()
              .getFirst();
    }
  }
}
