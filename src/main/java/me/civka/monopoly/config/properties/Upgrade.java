package me.civka.monopoly.config.properties;

import java.util.List;
import me.civka.monopoly.common.Requirement;

public record Upgrade(
    String name, int price, int gos, int tourism, int gpt, List<Requirement> requirements) {}
