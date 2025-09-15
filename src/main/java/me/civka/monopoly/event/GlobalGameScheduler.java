package me.civka.monopoly.event;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.event.GameTimer.TimerType;
import me.civka.monopoly.message.TimerMessage;
import me.civka.monopoly.service.GameService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GlobalGameScheduler {

  private static final int TURN_TIMER_SECONDS = 60;
  private static final int DICE_TIMER_SECONDS = 5;

  private final SimpMessagingTemplate messagingTemplate;
  private final GameService gameService;

  private static final Map<GameTimer, Integer> games = new ConcurrentHashMap<>();

  @Scheduled(fixedRate = 1000)
  public void tick() {
    games.forEach(
        (timer, timeLeft) -> {
          if (timeLeft > 0) {
            games.put(timer, --timeLeft);
            notifyWithUpdatedTimer(timer, timeLeft);
          } else {
            if (timer.getType() == TimerType.TURN) {
              gameService.forceEndTurn(timer.getRoomReference());
            } else {
              gameService.forceRollDice(timer.getRoomReference());
            }
          }
        });
  }

  public static void delegateTimer(UUID roomReference) {
    for (Entry<GameTimer, Integer> entry : games.entrySet()) {
      GameTimer timer = entry.getKey();

      if (timer.getRoomReference().equals(roomReference)) {
        TimerType newType =
            timer.getType() == GameTimer.TimerType.TURN
                ? GameTimer.TimerType.DICE
                : GameTimer.TimerType.TURN;

        timer.setType(newType);
        games.put(timer, newType == TimerType.TURN ? TURN_TIMER_SECONDS : DICE_TIMER_SECONDS);
        break;
      }
    }
  }

  public static void createTimer(UUID roomReference) {
    games.put(new GameTimer(roomReference, TimerType.DICE), DICE_TIMER_SECONDS);
  }

  private void notifyWithUpdatedTimer(GameTimer timer, int timeLeft) {
    messagingTemplate.convertAndSend(
        "/topic/games/" + timer.getRoomReference(), TimerMessage.of(timeLeft, timer.getType()));
  }
}
