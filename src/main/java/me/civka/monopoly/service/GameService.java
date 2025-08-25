package me.civka.monopoly.service;

import me.civka.monopoly.dto.room.RoomDto;

public interface GameService {

  RoomDto startGame();

  RoomDto rollDice();

  RoomDto endTurn();
}
