package me.civka.monopoly.dto.event;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.civka.monopoly.repository.entity.EventExtraData;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class EventDto {

  private UUID reference;
  private String type;
  private EventExtraData ext;
}
