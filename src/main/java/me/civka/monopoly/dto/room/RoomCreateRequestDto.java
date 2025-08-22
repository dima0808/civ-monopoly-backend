package me.civka.monopoly.dto.room;

import jakarta.validation.constraints.Min;
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
public class RoomCreateRequestDto {

  @NotBlank(message = "Name is mandatory")
  private String name;

  @Min(value = 2, message = "Member limit must be at least 2")
  private Integer memberLimit;

  @Size(min = 3, max = 20, message = "Password must be between 1 and 20 characters")
  private String password;
}
