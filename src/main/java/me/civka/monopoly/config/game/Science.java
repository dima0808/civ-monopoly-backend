package me.civka.monopoly.config.game;

/**
 * Science-project configuration.
 *
 * @param basicTurnAmount turns set on {@code turnsToNextScienceProject} after each science project
 * @param expeditionTurnAmount turns set on {@code expeditionTurns} when EXOPLANET completes
 * @param laserBoost turns subtracted from {@code expeditionTurns} when LASER completes
 * @param cost gold cost to perform a science project
 */
public record Science(
    Integer basicTurnAmount, Integer expeditionTurnAmount, Integer laserBoost, Integer cost) {}
