package me.civka.monopoly.service.mapper;

import me.civka.monopoly.dto.user.UserDto;
import me.civka.monopoly.dto.user.UserStatsDto;
import me.civka.monopoly.repository.entity.User;
import me.civka.monopoly.repository.entity.UserStats;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = RoomMapper.class)
public interface UserMapper {

  @Mapping(target = "room", ignore = true)
  UserDto toUserDto(User user);

  @Mapping(target = "room", source = "member.room", qualifiedByName = "toRoomWithoutMembersDto")
  UserDto toUserWithRoomDto(User user);

  UserStatsDto toUserStatsDto(UserStats stats);
}
