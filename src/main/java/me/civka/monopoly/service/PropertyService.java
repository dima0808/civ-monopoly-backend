package me.civka.monopoly.service;

import java.util.List;
import java.util.UUID;
import me.civka.monopoly.dto.property.PropertyDto;
import me.civka.monopoly.dto.property.PropertyRequestDto;
import me.civka.monopoly.dto.property.UpgradePropertyRequestDto;

public interface PropertyService {

  List<PropertyDto> getPropertiesByRoom(UUID roomReference);

  PropertyDto buyProperty(PropertyRequestDto buyRequest);

  PropertyDto upgradeProperty(UpgradePropertyRequestDto upgradeRequest);

  PropertyDto mortgageProperty(PropertyRequestDto mortgageRequest);

  PropertyDto demoteUpgrade(PropertyRequestDto demoteRequest);

  PropertyDto buybackProperty(PropertyRequestDto buybackRequest);

  PropertyDto payRent(PropertyRequestDto payRentRequest);
}
