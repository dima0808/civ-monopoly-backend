package me.civka.monopoly.config.game;

import java.util.Map;
import me.civka.monopoly.repository.entity.Property.BonusType;
import me.civka.monopoly.repository.entity.Property.UpgradeType;

public record Property(
    String name, Map<UpgradeType, Upgrade> upgrades, Map<BonusType, Bonus> bonuses) {}
