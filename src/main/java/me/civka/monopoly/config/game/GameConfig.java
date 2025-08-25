package me.civka.monopoly.config.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameConfig {

  private static final String PROPERTIES_CONFIGURATION_PATH = "src/main/resources/properties.json";

  @Bean
  public PropertiesConfiguration propertiesConfiguration() throws IOException {
    return new ObjectMapper()
        .readValue(new File(PROPERTIES_CONFIGURATION_PATH), PropertiesConfiguration.class);
  }
}
