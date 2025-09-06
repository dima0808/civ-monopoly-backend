package me.civka.monopoly.config.game;

import java.util.Map;
import me.civka.monopoly.common.Era;

public record GameConfiguration(Map<Era, Integer> eras) {}
