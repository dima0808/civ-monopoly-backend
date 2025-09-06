package me.civka.monopoly.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import me.civka.monopoly.config.game.GameConfiguration;
import me.civka.monopoly.config.properties.PropertiesConfiguration;
import me.civka.monopoly.config.requirements.RequirementsConfiguration;

public class ConfigurationHolder {

  public static final String GAME_CONFIGURATION_PATH = "src/main/resources/game.json";
  private static final String PROPERTIES_CONFIGURATION_PATH = "src/main/resources/properties.json";
  public static final String REQUIREMENTS_CONFIGURATION_PATH =
      "src/main/resources/requirements.json";

  private static final GameConfiguration gameConfiguration;
  private static final PropertiesConfiguration propertiesConfiguration;
  private static final RequirementsConfiguration requirementsConfiguration;

  static {
    try {
      gameConfiguration =
          new ObjectMapper().readValue(new File(GAME_CONFIGURATION_PATH), GameConfiguration.class);
      propertiesConfiguration =
          new ObjectMapper()
              .readValue(new File(PROPERTIES_CONFIGURATION_PATH), PropertiesConfiguration.class);
      requirementsConfiguration =
          new ObjectMapper()
              .readValue(
                  new File(REQUIREMENTS_CONFIGURATION_PATH), RequirementsConfiguration.class);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load configuration", e);
    }
  }

  public static GameConfiguration gameConfiguration() {
    return gameConfiguration;
  }

  public static PropertiesConfiguration propertiesConfiguration() {
    return propertiesConfiguration;
  }

  public static RequirementsConfiguration requirementsConfiguration() {
    return requirementsConfiguration;
  }
}
