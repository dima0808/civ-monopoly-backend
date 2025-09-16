package me.civka.monopoly.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.game.CivilizationListDto;
import me.civka.monopoly.dto.game.ColorListDto;
import me.civka.monopoly.dto.room.RoomDto;
import me.civka.monopoly.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
public class GameController {

  private final GameService gameService;

  @Operation(summary = "Get the list of available civilizations")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "List of civilizations retrieved"),
      })
  @GetMapping("/civilizations")
  public CivilizationListDto getAllCivilizations() {
    return gameService.getAllCivilizations();
  }

  @Operation(summary = "Get the list of available colors")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "List of colors retrieved"),
      })
  @GetMapping("/colors")
  public ColorListDto getAllColors() {
    return gameService.getAllColors();
  }

  @Operation(summary = "Start the game")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Game started successfully"),
        @ApiResponse(responseCode = "403", description = "User is not an owner of the room")
      })
  @PostMapping("/start")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public RoomDto startGame() {
    return gameService.startGame();
  }

  @Operation(summary = "Roll the dice")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Dice rolled successfully"),
        @ApiResponse(
            responseCode = "403",
            description = "It's not user's turn / User already rolled the dice this turn")
      })
  @PostMapping("/roll-dice")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public RoomDto rollDice() {
    return gameService.rollDice();
  }

  @Operation(summary = "End the current turn")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Turn ended successfully"),
        @ApiResponse(responseCode = "403", description = "It's not user's turn")
      })
  @PostMapping("/end-turn")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public RoomDto endTurn() {
    return gameService.endTurn();
  }
}
