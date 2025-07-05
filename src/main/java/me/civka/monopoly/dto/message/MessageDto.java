package me.civka.monopoly.dto.message;

import java.time.OffsetDateTime;
import java.util.UUID;
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
public class MessageDto {

  private UUID reference;
  private String message;
  private OffsetDateTime timeStamp;
  private String sender;
}
