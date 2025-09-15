package me.civka.monopoly.message;

import me.civka.monopoly.event.GameTimer.TimerType;

public record TimerMessage(int timeLeft, MessageType type) {

  public static TimerMessage of(int timeLeft, TimerType timerType) {
    return new TimerMessage(
        timeLeft,
        timerType == TimerType.TURN
            ? MessageType.TIMER_TURN_UPDATE
            : MessageType.TIMER_DICE_UPDATE);
  }

  public enum MessageType {
    TIMER_TURN_UPDATE,
    TIMER_DICE_UPDATE
  }
}
