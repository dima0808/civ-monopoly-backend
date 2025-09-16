package me.civka.monopoly.repository.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserStats {

  private Integer elo = 0;
  private Integer gamesPlayed = 0;
  private Integer gamesWon = 0;
  private Double averagePlace = 0.0;
}
