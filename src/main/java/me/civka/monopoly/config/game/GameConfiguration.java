package me.civka.monopoly.config.game;

import java.util.List;
import java.util.Map;
import me.civka.monopoly.common.AdditionalEffectType;
import me.civka.monopoly.common.Era;
import me.civka.monopoly.common.ProjectType;

public record GameConfiguration(
    Integer maxTurns,
    Integer militaryTarget,
    Integer goldForPassingRound,
    Integer tourismAdditionalThreshold,
    Map<Era, Integer> eras,
    List<ArmySpending> armySpending,
    Science science,
    Concert concert,
    Map<AdditionalEffectType, Integer> additionalGoldPerTurn,
    Map<ProjectType, Map<String, Integer>> projectEffects) {}
