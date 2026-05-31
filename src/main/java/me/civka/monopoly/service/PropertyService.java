package me.civka.monopoly.service;

import me.civka.monopoly.dto.property.PropertyDto;
import me.civka.monopoly.dto.property.PropertyRequestDto;
import me.civka.monopoly.dto.property.UpgradePropertyRequestDto;

public interface PropertyService {

  PropertyDto buyProperty(PropertyRequestDto buyRequest);

  PropertyDto upgradeProperty(UpgradePropertyRequestDto upgradeRequest);

  PropertyDto mortgageProperty(PropertyRequestDto mortgageRequest);

  PropertyDto demoteUpgrade(PropertyRequestDto demoteRequest);

  PropertyDto buybackProperty(PropertyRequestDto buybackRequest);

  PropertyDto payRent(PropertyRequestDto payRentRequest);
}
