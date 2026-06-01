package me.civka.monopoly.config.game;

import java.util.List;
import java.util.Map;
import me.civka.monopoly.common.Era;

public record GameConfiguration(
    Integer goldForPassingRound, Map<Era, Integer> eras, List<ArmySpending> armySpending) {}
