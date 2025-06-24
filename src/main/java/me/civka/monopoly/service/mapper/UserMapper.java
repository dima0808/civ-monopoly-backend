package me.civka.monopoly.service.mapper;

import me.civka.monopoly.dto.user.UserDto;
import me.civka.monopoly.repository.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

  UserDto toUserDto(User user);
}
