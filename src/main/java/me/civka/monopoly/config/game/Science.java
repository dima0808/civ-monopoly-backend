package me.civka.monopoly.config.game;

/**
 * Science-project configuration.
 *
 * @param basicTurnAmount turns set on {@code turnsToNextScienceProject} after each science project
 * @param expeditionTurnAmount turns set on {@code expeditionTurns} when EXOPLANET completes
 * @param laserBoost turns subtracted from {@code expeditionTurns} when LASER completes
 * @param cost gold cost to perform a science project
 * @param labBoost turns subtracted per Campus at LEVEL_4
 * @param governmentBoost turns subtracted per Government Plaza at LEVEL_4_1
 * @param oxfordBoost turns subtracted if Oxford University (pos 46) is owned
 * @param spaceportBoost turns subtracted if Spaceport (pos 47) is owned
 */
public record Science(
    Integer basicTurnAmount,
    Integer expeditionTurnAmount,
    Integer laserBoost,
    Integer cost,
    Integer labBoost,
    Integer governmentBoost,
    Integer oxfordBoost,
    Integer spaceportBoost) {}
