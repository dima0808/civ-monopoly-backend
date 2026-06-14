package me.civka.monopoly.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.civka.monopoly.config.ConfigurationHolder;
import me.civka.monopoly.config.properties.Bonus;
import me.civka.monopoly.config.properties.PropertiesConfiguration;
import me.civka.monopoly.config.properties.PropertyDetails;
import me.civka.monopoly.repository.PropertyRepository;
import me.civka.monopoly.repository.entity.Property;
import me.civka.monopoly.repository.entity.Property.BonusType;
import me.civka.monopoly.repository.entity.Property.UpgradeType;
import org.springframework.stereotype.Service;

@Service
public class BonusService {

  private static final Map<BonusType, Integer> WONDER_POSITIONS =
      Map.of(
          BonusType.TEMPLE_OF_ARTEMIS, 4,
          BonusType.COLOSSEUM, 23,
          BonusType.ETEMENANKI, 27,
          BonusType.MAUSOLEUM_AT_HALICARNASSUS, 32,
          BonusType.RUHR_VALLEY, 36,
          BonusType.ESTADIO_DO_MARACANA, 40);

  private static final List<Integer> FARM_POSITIONS = List.of(25, 26, 28);

  private final PropertiesConfiguration propertiesConfiguration =
      ConfigurationHolder.propertiesConfiguration();

  private final PropertyRepository propertyRepository;

  public BonusService(PropertyRepository propertyRepository) {
    this.propertyRepository = propertyRepository;
  }

  /**
   * Recalculates all bonuses for the given member properties. Returns properties whose bonuses
   * changed (excluding the property at {@code triggerPosition}, which the caller already
   * broadcasts).
   */
  public List<Property> recalculateBonuses(List<Property> memberProperties, int triggerPosition) {
    Map<Integer, Property> propertyByPosition = new HashMap<>();
    for (Property p : memberProperties) {
      propertyByPosition.put(p.getPosition(), p);
    }

    List<Property> changed = new ArrayList<>();

    for (Property property : memberProperties) {
      List<BonusType> oldBonuses = List.copyOf(property.getBonuses());
      List<BonusType> newBonuses = computeBonuses(property, propertyByPosition);
      property.setBonuses(newBonuses);

      if (!oldBonuses.equals(newBonuses)) {
        propertyRepository.save(property);
        if (property.getPosition() != triggerPosition) {
          changed.add(property);
        }
      }
    }

    return changed;
  }

  private List<BonusType> computeBonuses(Property property, Map<Integer, Property> propMap) {
    PropertyDetails details = propertiesConfiguration.properties().get(property.getPosition());
    if (details == null || details.bonuses() == null || details.bonuses().isEmpty()) {
      return new ArrayList<>();
    }

    if (property.getMortgage() != -1) {
      return new ArrayList<>();
    }

    List<BonusType> bonuses = new ArrayList<>();
    for (Map.Entry<BonusType, Bonus> entry : details.bonuses().entrySet()) {
      BonusType bonusType = entry.getKey();
      Bonus bonus = entry.getValue();
      if (isBonusActive(bonusType, bonus, property, propMap)) {
        bonuses.add(bonusType);
      }
    }
    return bonuses;
  }

  private boolean isBonusActive(
      BonusType bonusType, Bonus bonus, Property property, Map<Integer, Property> propMap) {
    if (bonus.type() == 0) {
      return isWonderBonusActive(bonusType, property, propMap);
    }
    return isAdjacencyBonusActive(bonusType, property, propMap);
  }

  // -- Wonder bonuses (type 0) --

  private boolean isWonderBonusActive(
      BonusType bonusType, Property property, Map<Integer, Property> propMap) {
    if (bonusType == BonusType.CASA_DE_CONTRATACION) {
      return isCasaActive(property, propMap);
    }

    Integer wonderPosition = WONDER_POSITIONS.get(bonusType);
    if (wonderPosition == null) {
      return false;
    }

    if (!hasProperty(propMap, wonderPosition)) {
      return false;
    }

    // Temple of Artemis: target must have LEVEL_2
    if (bonusType == BonusType.TEMPLE_OF_ARTEMIS) {
      return property.getUpgrades().contains(UpgradeType.LEVEL_2);
    }

    // Ruhr Valley: Iron at position 11 must have LEVEL_2
    if (bonusType == BonusType.RUHR_VALLEY && property.getPosition() == 11) {
      return property.getUpgrades().contains(UpgradeType.LEVEL_2);
    }

    return true;
  }

  private boolean isCasaActive(Property property, Map<Integer, Property> propMap) {
    if (!hasProperty(propMap, 20)) {
      return false;
    }

    int pos = property.getPosition();

    // Gov Plaza at 9 -> properties at position >= 14
    if (hasProperty(propMap, 9) && pos >= 14) {
      return true;
    }

    // Gov Plaza at 18 -> properties at position <= 12 OR >= 25
    if (hasProperty(propMap, 18) && (pos <= 12 || pos >= 25)) {
      return true;
    }

    // Gov Plaza at 44 -> properties at position <= 36
    if (hasProperty(propMap, 44) && pos <= 36) {
      return true;
    }

    return false;
  }

  // -- Adjacency bonuses (type 1) --

  private boolean isAdjacencyBonusActive(
      BonusType bonusType, Property property, Map<Integer, Property> propMap) {
    int pos = property.getPosition();

    return switch (bonusType) {
      case GOVERNMENT_PLAZA ->
          (pos == 10 && hasProperty(propMap, 9))
              || ((pos == 17 || pos == 19) && hasProperty(propMap, 18))
              || ((pos == 43 || pos == 45) && hasProperty(propMap, 44));
      case IRON -> pos == 10 && hasProperty(propMap, 11);
      case FABRIC ->
          pos == 11
              && property.getUpgrades().contains(UpgradeType.LEVEL_2)
              && hasPropertyWithLevel(propMap, UpgradeType.LEVEL_3, 10, 34);
      case SHIPYARD ->
          (pos == 12 || pos == 14) && hasPropertyWithLevel(propMap, UpgradeType.LEVEL_3, 17, 31);
      case REEF -> pos == 15 && hasProperty(propMap, 14);
      case WONDER ->
          (pos == 21 && hasProperty(propMap, 20))
              || (pos == 39 && hasProperty(propMap, 40))
              || (pos == 41 && (hasProperty(propMap, 40) || hasProperty(propMap, 42)));
      case ENTERTAINMENT_COMPLEX ->
          (pos == 21 && hasProperty(propMap, 22))
              || (pos == 39 && hasProperty(propMap, 38));
      case FARMS -> isFarmsActive(pos, property, propMap);
      case AQUEDUCT -> pos == 34 && hasProperty(propMap, 33);
      case DAM -> pos == 34 && hasProperty(propMap, 35);
      default -> false;
    };
  }

  private boolean isFarmsActive(int pos, Property property, Map<Integer, Property> propMap) {
    if (!FARM_POSITIONS.contains(pos)) {
      return false;
    }
    if (!property.getUpgrades().contains(UpgradeType.LEVEL_2)) {
      return false;
    }

    // At least one other farm position must have LEVEL_2
    return FARM_POSITIONS.stream()
        .filter(p -> p != pos)
        .anyMatch(p -> hasPropertyWithLevel(propMap, UpgradeType.LEVEL_2, p));
  }

  // -- Helpers --

  private boolean hasProperty(Map<Integer, Property> propMap, int position) {
    Property p = propMap.get(position);
    return p != null && p.getMortgage() == -1;
  }

  private boolean hasPropertyWithLevel(
      Map<Integer, Property> propMap, UpgradeType level, int... positions) {
    for (int pos : positions) {
      Property p = propMap.get(pos);
      if (p != null && p.getMortgage() == -1 && p.getUpgrades().contains(level)) {
        return true;
      }
    }
    return false;
  }
}
