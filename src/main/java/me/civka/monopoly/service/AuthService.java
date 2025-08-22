package me.civka.monopoly.service;

import me.civka.monopoly.dto.user.UserDto;
import me.civka.monopoly.dto.user.UserJwtTokenDto;
import me.civka.monopoly.dto.user.UserRequestDto;
import org.springframework.security.core.Authentication;

public interface AuthService {

  UserJwtTokenDto register(UserRequestDto userRequestDto);

  UserJwtTokenDto login(UserRequestDto userRequestDto);

  Authentication authenticate(String authorizationHeader);

  UserDto getCurrentUser();
}
