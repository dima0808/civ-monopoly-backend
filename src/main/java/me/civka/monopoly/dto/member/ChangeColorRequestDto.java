package me.civka.monopoly.dto.member;

import me.civka.monopoly.repository.entity.Member.Color;
import me.civka.monopoly.validation.EnumValueMatch;

public record ChangeColorRequestDto(@EnumValueMatch(enumClass = Color.class) String color) {}
