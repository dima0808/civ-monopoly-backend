package me.civka.monopoly.dto.member;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class MemberDto {

  private UUID reference;
  private String civilization;
  private String color;
  private Integer position;
  private Integer gold;
  private Integer strength;
  private Integer tourism;
  private Integer score;
  private List<String> finishedScienceProjects;
  private Integer turnsToNextScienceProject;
  private Integer expeditionTurns;
  private List<AdditionalEffectDto> additionalEffects;
  private String username;
  private String avatarUrl;
}
