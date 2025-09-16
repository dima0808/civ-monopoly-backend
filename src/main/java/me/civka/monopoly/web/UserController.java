package me.civka.monopoly.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.user.UserDto;
import me.civka.monopoly.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

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
    return userService.getCurrentUser();
  }

  @Operation(summary = "Get user by username")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "User details retrieved"),
        @ApiResponse(responseCode = "404", description = "User not found")
      })
  @GetMapping("/{username}")
  @ResponseStatus(HttpStatus.OK)
  public UserDto getUserByUsername(@PathVariable String username) {
    return userService.getUserByUsername(username);
  }

  @Operation(summary = "Update user avatar")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Avatar updated successfully"),
        @ApiResponse(responseCode = "403", description = "User is not authorized to update avatar"),
      })
  @PutMapping("/avatar")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public UserDto updateAvatar(@RequestParam MultipartFile file) {
    return userService.updateAvatar(file);
  }
}
