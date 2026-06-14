package me.civka.monopoly.repository.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Setter
@Getter
@NoArgsConstructor
public class EventExtraData {

  private Integer roll = 0;
  private Integer teleportOption1;
  private Integer teleportOption2;
  private Integer teleportOption3;

  public EventExtraData(Integer roll) {
    this.roll = roll;
  }

  public EventExtraData(Integer teleportOption1, Integer teleportOption2, Integer teleportOption3) {
    this.teleportOption1 = teleportOption1;
    this.teleportOption2 = teleportOption2;
    this.teleportOption3 = teleportOption3;
  }
}
