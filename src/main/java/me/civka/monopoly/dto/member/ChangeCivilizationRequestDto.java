package me.civka.monopoly.dto.member;

import me.civka.monopoly.repository.entity.Member.Civilization;
import me.civka.monopoly.validation.EnumValueMatch;

public record ChangeCivilizationRequestDto(
    @EnumValueMatch(enumClass = Civilization.class) String civilization) {}
