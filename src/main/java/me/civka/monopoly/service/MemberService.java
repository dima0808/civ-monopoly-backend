package me.civka.monopoly.service;

import me.civka.monopoly.dto.member.ChangeCivilizationRequestDto;
import me.civka.monopoly.dto.member.ChangeColorRequestDto;
import me.civka.monopoly.dto.member.MemberDto;

public interface MemberService {

  MemberDto changeCivilization(ChangeCivilizationRequestDto requestDto);

  MemberDto changeColor(ChangeColorRequestDto requestDto);
}
