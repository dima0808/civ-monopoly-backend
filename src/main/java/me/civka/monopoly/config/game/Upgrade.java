package me.civka.monopoly.config.game;

import java.util.List;
import me.civka.monopoly.common.Requirement;

public record Upgrade(String name, int price, int gos, int gpt, List<Requirement> requirements) {}
