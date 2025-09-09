package me.civka.monopoly.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserRequestDto {

  @NotBlank(message = "Username is mandatory")
  @Size(min = 3, max = 16, message = "Username must be between 3 and 16 characters")
  private String username;

  @NotBlank(message = "Password is mandatory")
  @Size(min = 6, max = 35, message = "Password must be between 6 and 35 characters")
  private String password;
}
