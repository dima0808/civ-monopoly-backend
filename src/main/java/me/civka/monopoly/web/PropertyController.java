package me.civka.monopoly.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.property.PropertyDto;
import me.civka.monopoly.dto.property.PropertyRequestDto;
import me.civka.monopoly.dto.property.UpgradePropertyRequestDto;
import me.civka.monopoly.service.PropertyService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

  @Operation(summary = "Get all properties in a room")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Properties retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Room not found")
      })
  @GetMapping("/room/{roomReference}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public List<PropertyDto> getPropertiesByRoom(
      @Parameter(description = "Room reference UUID", required = true) @PathVariable
          UUID roomReference) {
    return propertyService.getPropertiesByRoom(roomReference);
  }

  @Operation(summary = "Buy property")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "PropertyDetails bought successfully"),
        @ApiResponse(
            responseCode = "403",
            description = "Unable to buy the property (for any reason)")
      })
  @PostMapping("/buy")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public PropertyDto buyProperty(
      @Parameter(description = "PropertyDetails buy request", required = true) @RequestBody @Valid
          PropertyRequestDto buyRequest) {
    return propertyService.buyProperty(buyRequest);
  }

  @Operation(summary = "Upgrade property")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "PropertyDetails upgraded successfully"),
        @ApiResponse(
            responseCode = "403",
            description = "Unable to upgrade the property (for any reason)")
      })
  @PostMapping("/upgrade")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public PropertyDto upgradeProperty(
      @Parameter(description = "PropertyDetails position", required = true) @RequestBody @Valid
          UpgradePropertyRequestDto upgradeRequest) {
    return propertyService.upgradeProperty(upgradeRequest);
  }

  @Operation(summary = "Mortgage property")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "PropertyDetails mortgaged successfully"),
        @ApiResponse(
            responseCode = "403",
            description = "Unable to mortgage the property (for any reason)")
      })
  @PostMapping("/mortgage")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public PropertyDto mortgageProperty(
      @Parameter(description = "PropertyDetails mortgage request", required = true)
          @RequestBody
          @Valid
          PropertyRequestDto mortgageRequest) {
    return propertyService.mortgageProperty(mortgageRequest);
  }

  @Operation(summary = "Demote property upgrade")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "PropertyDetails upgrade demoted successfully"),
        @ApiResponse(
            responseCode = "403",
            description = "Unable to demote the property upgrade (for any reason)")
      })
  @PostMapping("/demote")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public PropertyDto demoteUpgrade(
      @Parameter(description = "PropertyDetails demote request", required = true)
          @RequestBody
          @Valid
          PropertyRequestDto demoteRequest) {
    return propertyService.demoteUpgrade(demoteRequest);
  }

  @Operation(summary = "Pay rent on foreign property")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Rent paid successfully"),
        @ApiResponse(responseCode = "403", description = "Unable to pay rent (for any reason)")
      })
  @PostMapping("/pay-rent")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public PropertyDto payRent(
      @Parameter(description = "Pay rent request", required = true) @RequestBody @Valid
          PropertyRequestDto payRentRequest) {
    return propertyService.payRent(payRentRequest);
  }

  @Operation(summary = "Buyback mortgaged property")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "PropertyDetails buyback successful"),
        @ApiResponse(
            responseCode = "403",
            description = "Unable to buyback the property (for any reason)")
      })
  @PostMapping("/buyback")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public PropertyDto buybackProperty(
      @Parameter(description = "PropertyDetails buyback request", required = true)
          @RequestBody
          @Valid
          PropertyRequestDto buybackRequest) {
    return propertyService.buybackProperty(buybackRequest);
  }
}
