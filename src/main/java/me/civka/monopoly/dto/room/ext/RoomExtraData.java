package me.civka.monopoly.dto.room.ext;

public record RoomExtraData(DiceResult diceResult) {

  public static RoomExtraData of(DiceResult diceResult) {
    return new RoomExtraData(diceResult);
  }
}
