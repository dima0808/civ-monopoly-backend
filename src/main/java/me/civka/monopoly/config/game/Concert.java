package me.civka.monopoly.config.game;

/**
 * Concert (culture project) configuration.
 *
 * @param cost gold cost to perform a concert
 * @param tourismLowerBound minimum tourism granted
 * @param tourismUpperBound maximum tourism granted
 */
public record Concert(Integer cost, Integer tourismLowerBound, Integer tourismUpperBound) {}
