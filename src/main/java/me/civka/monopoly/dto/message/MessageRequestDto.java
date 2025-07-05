package me.civka.monopoly.dto.message;

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
public class MessageRequestDto {

  @NotBlank
  @Size(min = 1, max = 50, message = "Message must be between 1 and 50 characters")
  private String message;
}
