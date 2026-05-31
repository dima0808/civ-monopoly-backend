package me.civka.monopoly.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.civka.monopoly.common.PropertyType;
import me.civka.monopoly.config.ConfigurationHolder;
import me.civka.monopoly.config.properties.PropertyDetails;
import me.civka.monopoly.repository.entity.Property;

public class PropertyUtils {

  public static final String DEER_PROPERTY_NAME = "Deer";
  public static final String FURS_PROPERTY_NAME = "Furs";
  public static final String ENCAMPMENT_PROPERTY_NAME = "Encampment";
  public static final String HARBOR_PROPERTY_NAME = "Harbor";
  public static final String SPACEPORT_PROPERTY_NAME = "Spaceport";
  public static final String CAMPUS_PROPERTY_NAME = "Campus";
  public static final String GOVERNMENT_PLAZA_PROPERTY_NAME = "Government Plaza";
  public static final String ENTERTAINMENT_COMPLEX_PROPERTY_NAME = "Entertainment Complex";
  public static final String INDUSTRIAL_ZONE_PROPERTY_NAME = "Industrial Zone";
  public static final String COMMERCIAL_HUB_PROPERTY_NAME = "Commercial Hub";

  private static final Map<Integer, PropertyDetails> properties =
      ConfigurationHolder.propertiesConfiguration().properties();

  public static List<Integer> getPositionByName(String name) {
    List<Integer> positions =
        properties.entrySet().stream()
            .filter(entry -> entry.getValue().name().equals(name))
            .map(Entry::getKey)
            .toList();

    if (positions.isEmpty()) {
      throw new IllegalArgumentException("No property found with name: " + name);
    }
    return positions;
  }

  public static List<Integer> getPositionByType(PropertyType type) {
    List<Integer> positions =
        properties.entrySet().stream()
            .filter(entry -> entry.getValue().type() == type)
            .map(Entry::getKey)
            .toList();

    if (positions.isEmpty()) {
      throw new IllegalArgumentException("No property found with type: " + type);
    }
    return positions;
  }

  public static int calculateGoldOnStep(Property property) {
    if (property.getMortgage() != -1) {
      return 0;
    }
    PropertyDetails propertyDetail = properties.get(property.getPosition());
    int gos =
        property.getUpgrades().stream().mapToInt(u -> propertyDetail.upgrades().get(u).gos()).sum();
    gos +=
        property.getBonuses().stream().mapToInt(b -> propertyDetail.bonuses().get(b).gos()).sum();
    return gos;
  }

  public static int calculateTourismOnStep(Property property) {
    if (property.getMortgage() != -1) {
      return 0;
    }
    PropertyDetails propertyDetail = properties.get(property.getPosition());
    int tourism =
        property.getUpgrades().stream()
            .mapToInt(u -> propertyDetail.upgrades().get(u).tourism())
            .sum();
    tourism +=
        property.getBonuses().stream()
            .mapToInt(b -> propertyDetail.bonuses().get(b).tourism())
            .sum();
    return tourism;
  }

  public static int calculateGpt(List<Property> ownedProperties) {
    Map<Integer, PropertyDetails> properties =
        ConfigurationHolder.propertiesConfiguration().properties();

    int gpt = 0;
    for (Property property : ownedProperties) {
      PropertyDetails propertyDetail = properties.get(property.getPosition());
      gpt +=
          property.getUpgrades().stream()
              .mapToInt(u -> propertyDetail.upgrades().get(u).gpt())
              .sum();
      gpt +=
          property.getBonuses().stream().mapToInt(b -> propertyDetail.bonuses().get(b).gpt()).sum();
    }

    return gpt;
  }
}
