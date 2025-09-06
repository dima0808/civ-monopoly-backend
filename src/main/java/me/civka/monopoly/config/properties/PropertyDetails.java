package me.civka.monopoly.config.properties;

import java.util.Map;
import me.civka.monopoly.common.PropertyType;
import me.civka.monopoly.repository.entity.Property.BonusType;
import me.civka.monopoly.repository.entity.Property.UpgradeType;

public record PropertyDetails(
    String name,
    PropertyType type,
    Map<UpgradeType, Upgrade> upgrades,
    Map<BonusType, Bonus> bonuses) {}
