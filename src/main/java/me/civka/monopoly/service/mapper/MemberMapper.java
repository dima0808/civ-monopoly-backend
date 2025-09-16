package me.civka.monopoly.service.mapper;

import java.util.List;
import me.civka.monopoly.dto.member.MemberDto;
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
}
