package me.civka.monopoly.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.property.PropertyDto;
import me.civka.monopoly.dto.property.PropertyRequestDto;
import me.civka.monopoly.dto.property.UpgradePropertyRequestDto;
import me.civka.monopoly.service.PropertyService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyController {

  private final PropertyService propertyService;

  @Operation(summary = "Buy property")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Property bought successfully"),
        @ApiResponse(
            responseCode = "403",
            description = "Unable to buy the property (for any reason)")
      })
  @PostMapping("/buy")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public PropertyDto buyProperty(
      @Parameter(description = "Property buy request", required = true) @RequestBody @Valid
          PropertyRequestDto buyRequest) {
    return propertyService.buyProperty(buyRequest);
  }

  @Operation(summary = "Upgrade property")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Property upgraded successfully"),
        @ApiResponse(
            responseCode = "403",
            description = "Unable to upgrade the property (for any reason)")
      })
  @PostMapping("/upgrade")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public PropertyDto upgradeProperty(
      @Parameter(description = "Property position", required = true) @RequestBody @Valid
          UpgradePropertyRequestDto upgradeRequest) {
    return propertyService.upgradeProperty(upgradeRequest);
  }

  @Operation(summary = "Mortgage property")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Property mortgaged successfully"),
        @ApiResponse(
            responseCode = "403",
            description = "Unable to mortgage the property (for any reason)")
      })
  @PostMapping("/mortgage")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public PropertyDto mortgageProperty(
      @Parameter(description = "Property mortgage request", required = true) @RequestBody @Valid
          PropertyRequestDto mortgageRequest) {
    return propertyService.mortgageProperty(mortgageRequest);
  }

  @Operation(summary = "Demote property upgrade")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Property upgrade demoted successfully"),
        @ApiResponse(
            responseCode = "403",
            description = "Unable to demote the property upgrade (for any reason)")
      })
  @PostMapping("/demote")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public PropertyDto demoteUpgrade(
      @Parameter(description = "Property demote request", required = true) @RequestBody @Valid
          PropertyRequestDto demoteRequest) {
    return propertyService.demoteUpgrade(demoteRequest);
  }

  @Operation(summary = "Buyback mortgaged property")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Property buyback successful"),
        @ApiResponse(
            responseCode = "403",
            description = "Unable to buyback the property (for any reason)")
      })
  @PostMapping("/buyback")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public PropertyDto buybackProperty(
      @Parameter(description = "Property buyback request", required = true) @RequestBody @Valid
          PropertyRequestDto buybackRequest) {
    return propertyService.buybackProperty(buybackRequest);
  }
}
