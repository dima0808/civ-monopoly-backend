package me.civka.monopoly.service;

import me.civka.monopoly.dto.member.MemberDto;
import me.civka.monopoly.repository.entity.Member.Civilization;
import me.civka.monopoly.repository.entity.Member.Color;

public interface MemberService { // TODO

  MemberDto changeCivilization(Civilization civilization);

  MemberDto changeColor(Color color);
}
