package me.civka.monopoly.service;

import me.civka.monopoly.dto.game.CivilizationListDto;
import me.civka.monopoly.dto.game.ColorListDto;
import me.civka.monopoly.dto.room.RoomDto;

public interface GameService {

  CivilizationListDto getAllCivilizations();

  ColorListDto getAllColors();

  RoomDto startGame();

  RoomDto rollDice();

  RoomDto endTurn();
}
