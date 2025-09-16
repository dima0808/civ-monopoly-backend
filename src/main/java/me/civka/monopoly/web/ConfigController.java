package me.civka.monopoly.web;

import static me.civka.monopoly.config.ConfigurationHolder.gameConfiguration;
import static me.civka.monopoly.config.ConfigurationHolder.propertiesConfiguration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.config.game.GameConfiguration;
import me.civka.monopoly.config.properties.PropertiesConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/configs")
@RequiredArgsConstructor
public class ConfigController {

  @Operation(summary = "Get the properties configuration")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Properties configuration retrieved"),
      })
  @GetMapping("/properties")
  @ResponseStatus(HttpStatus.OK)
  public PropertiesConfiguration getPropertiesConfiguration() {
    return propertiesConfiguration();
  }

  @Operation(summary = "Get the game configuration")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Game configuration retrieved"),
      })
  @GetMapping("/game")
  @ResponseStatus(HttpStatus.OK)
  public GameConfiguration getGameConfiguration() {
    return gameConfiguration();
  }
}
