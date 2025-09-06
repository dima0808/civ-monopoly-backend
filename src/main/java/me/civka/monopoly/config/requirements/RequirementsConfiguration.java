package me.civka.monopoly.config.requirements;

import java.util.Map;
import me.civka.monopoly.common.Requirement;

public record RequirementsConfiguration(Map<Requirement, RequirementDetails> requirements) {}
