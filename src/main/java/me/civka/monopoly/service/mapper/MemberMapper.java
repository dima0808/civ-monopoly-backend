package me.civka.monopoly.service.mapper;

import java.util.List;
import java.util.Map;
import me.civka.monopoly.common.AdditionalEffectType;
import me.civka.monopoly.config.ConfigurationHolder;
import me.civka.monopoly.dto.member.AdditionalEffectDto;
import me.civka.monopoly.dto.member.MemberDto;
import me.civka.monopoly.repository.entity.AdditionalEffect;
import me.civka.monopoly.repository.entity.Member;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface MemberMapper {

  @Mapping(target = "username", source = "user.username")
  @Mapping(target = "avatarUrl", source = "user.avatarUrl")
  MemberDto toMemberDto(Member member);

  @Named("toMemberDto")
  List<MemberDto> toMemberDto(List<Member> members);

  // Gold-per-turn is config-derived from the effect type, so map effects manually.
  default List<AdditionalEffectDto> toAdditionalEffectDtos(List<AdditionalEffect> effects) {
    if (effects == null) {
      return null;
    }
    Map<AdditionalEffectType, Integer> goldPerTurn =
        ConfigurationHolder.gameConfiguration().additionalGoldPerTurn();
    return effects.stream()
        .map(
            effect ->
                AdditionalEffectDto.builder()
                    .type(effect.getType().name())
                    .turnsLeft(effect.getTurnsLeft())
                    .goldPerTurn(goldPerTurn.getOrDefault(effect.getType(), 0))
                    .build())
        .toList();
  }
}
