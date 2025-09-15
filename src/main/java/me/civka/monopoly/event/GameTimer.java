package me.civka.monopoly.event;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GameTimer {
  private final UUID roomReference;
  private TimerType type;

  public static GameTimer of(UUID roomReference, TimerType type) {
    return new GameTimer(roomReference, type);
  }

  public enum TimerType {
    TURN,
    DICE
  }
}
