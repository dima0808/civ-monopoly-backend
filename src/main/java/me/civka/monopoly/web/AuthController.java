package me.civka.monopoly.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.user.UserDto;
import me.civka.monopoly.dto.user.UserJwtTokenDto;
import me.civka.monopoly.dto.user.UserRequestDto;
import me.civka.monopoly.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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

  @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
        @ApiResponse(responseCode = "400", description = "Invalid credentials")
      })
  @PostMapping("/login")
  @ResponseStatus(HttpStatus.OK)
  public UserJwtTokenDto login(
      @Parameter(
              description = "User login request",
              required = true,
              content = @Content(schema = @Schema(implementation = UserRequestDto.class)))
          @Valid
          @RequestBody
          UserRequestDto userRequestDto) {
    return authService.login(userRequestDto);
  }

  @Operation(
      summary = "User registration",
      description = "Registers a new user and returns a JWT token.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "User registered"),
        @ApiResponse(responseCode = "400", description = "Invalid registration data")
      })
  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public UserJwtTokenDto register(
      @Parameter(
              description = "User registration request",
              required = true,
              content = @Content(schema = @Schema(implementation = UserRequestDto.class)))
          @Valid
          @RequestBody
          UserRequestDto userRequestDto) {
    return authService.register(userRequestDto);
  }

  @Operation(
      summary = "Get current user",
      description = "Returns the details of the currently authenticated user.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Current user details retrieved"),
        @ApiResponse(responseCode = "403", description = "User not authenticated")
      })
  @GetMapping("/current")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public UserDto getCurrentUser() {
    return authService.getCurrentUser();
  }
}
