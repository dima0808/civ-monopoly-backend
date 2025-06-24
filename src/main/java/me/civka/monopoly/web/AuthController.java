package me.civka.monopoly.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.user.UserJwtTokenDto;
import me.civka.monopoly.dto.user.UserRequestDto;
import me.civka.monopoly.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  @ResponseStatus(HttpStatus.OK)
  public UserJwtTokenDto login(@Valid @RequestBody UserRequestDto userRequestDto) {
    return authService.login(userRequestDto);
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public UserJwtTokenDto register(@Valid @RequestBody UserRequestDto userRequestDto) {
    return authService.register(userRequestDto);
  }
}
