package me.civka.monopoly.dto.user;

public record UserStatsDto(
    Integer elo, Integer gamesPlayed, Integer gamesWon, Double averagePlace) {}
