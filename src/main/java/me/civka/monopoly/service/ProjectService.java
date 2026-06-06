package me.civka.monopoly.service;

import java.util.List;
import me.civka.monopoly.dto.member.AdditionalEffectDto;
import me.civka.monopoly.dto.member.MemberDto;
import me.civka.monopoly.dto.project.ProjectChoiceRequestDto;

public interface ProjectService {

  MemberDto chooseProject(ProjectChoiceRequestDto request);

  MemberDto doScienceProject();

  MemberDto doConcert();

  List<AdditionalEffectDto> getMyAdditionalEffects();
}
