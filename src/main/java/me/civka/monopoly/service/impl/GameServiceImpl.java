package me.civka.monopoly.service.impl;

import static me.civka.monopoly.util.GameUtils.BOARD_SIZE;
import static me.civka.monopoly.util.GameUtils.getMemberFromAuthentication;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.game.CivilizationListDto;
import me.civka.monopoly.dto.game.ColorListDto;
import me.civka.monopoly.dto.room.RoomDto;
import me.civka.monopoly.dto.room.ext.DiceResult;
import me.civka.monopoly.dto.room.ext.RoomExtraData;
import me.civka.monopoly.repository.MemberRepository;
import me.civka.monopoly.repository.PropertyRepository;
import me.civka.monopoly.repository.RoomRepository;
import me.civka.monopoly.repository.entity.Member;
import me.civka.monopoly.repository.entity.Member.Civilization;
import me.civka.monopoly.repository.entity.Property;
import me.civka.monopoly.repository.entity.Room;
import me.civka.monopoly.service.GameService;
import me.civka.monopoly.service.exception.room.RoomNotFoundException;
import me.civka.monopoly.service.exception.user.UserNotAllowedException;
import me.civka.monopoly.service.mapper.RoomMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

  private static final int DICE_SIZE = 6;

  private final RoomRepository roomRepository;
  private final RoomMapper roomMapper;
  private final Random random = new Random();
  private final PropertyRepository propertyRepository;
  private final MemberRepository memberRepository;

  @Override
  public CivilizationListDto getAllCivilizations() {
    List<String> civilizations = Arrays.stream(Civilization.values()).map(Enum::toString).toList();
    return new CivilizationListDto(civilizations);
  }

  @Override
  public ColorListDto getAllColors() {
    List<String> colors = Arrays.stream(Member.Color.values()).map(Enum::toString).toList();
    return new ColorListDto(colors);
  }

  @Override
  public RoomDto startGame() {
    Member member = getMemberFromAuthentication();
    Room room =
        roomRepository
            .getRoomByMembersContaining(member)
            .orElseThrow(() -> new RoomNotFoundException(member));

    if (!room.isOwnedBy(member.getUser())) {
      throw new UserNotAllowedException("User are not allowed to start the properties.");
    }

    room.setIsStarted(true);
    assignRandomCivilizations(room);

    delegateNextTurn(room);

    return roomMapper.toRoomDto(roomRepository.save(room));
  }

  @Override
  public RoomDto rollDice() {
    Member member = getMemberFromAuthentication();
    Room room =
        roomRepository
            .getRoomByMembersContaining(member)
            .orElseThrow(() -> new RoomNotFoundException(member));

    if (!member.equalsById(room.getMembers().get(room.getTurnIndex()))) {
      throw new UserNotAllowedException("It is not user's turn.");
    }

    if (room.getIsDiceRolled()) {
      throw new UserNotAllowedException("User has already rolled the dice this turn.");
    }

    int firstRoll = random.nextInt(DICE_SIZE) + 1;
    int secondRoll = random.nextInt(DICE_SIZE) + 1;
    room.setIsDiceRolled(true);
    member.setPosition((member.getPosition() + firstRoll + secondRoll) % BOARD_SIZE);

    memberRepository.save(member);
    RoomDto roomDto = roomMapper.toRoomDto(roomRepository.save(room));
    roomDto.setExt(RoomExtraData.of(new DiceResult(firstRoll, secondRoll)));
    return roomDto;
  }

  @Override
  @Transactional
  public RoomDto endTurn() {
    Member member = getMemberFromAuthentication();
    Room room =
        roomRepository
            .getRoomByMembersContaining(member)
            .orElseThrow(() -> new RoomNotFoundException(member));

    if (!member.equalsById(room.getMembers().get(room.getTurnIndex()))) {
      throw new UserNotAllowedException("It is not user's turn.");
    }

    if (!room.getIsDiceRolled()) {
      throw new UserNotAllowedException("User must roll the dice before ending the turn.");
    }

    delegateNextTurn(room);

    return roomMapper.toRoomDto(roomRepository.save(room));
  }

  private void delegateNextTurn(Room room) {
    if (room.getTurnIndex() != -1) {
      room.setTurnIndex((room.getTurnIndex() + 1) % room.getMembers().size());
      if (room.getTurnIndex().equals(room.getState().getFirstTurnIndex())) {
        room.setTurn(room.getTurn() + 1);
        checkForMortgage(room);
      }
    } else {
      int firstTurnIndex = random.nextInt(room.getMembers().size());
      room.setTurnIndex(firstTurnIndex);
      room.getState().setFirstTurnIndex(firstTurnIndex);
    }
    room.setIsDiceRolled(false);
  }

  private void checkForMortgage(Room room) {
    List<Property> properties = propertyRepository.getPropertiesByRoom(room);

    properties.forEach(property -> property.setMortgage(property.getMortgage() - 1));
    propertyRepository.saveAll(properties);

    propertyRepository.deleteAllById(
        properties.stream()
            .filter(property -> property.getMortgage() == 0)
            .map(Property::getReference)
            .toList());
  }

  private void assignRandomCivilizations(Room room) {
    List<Civilization> takenCivilizations =
        room.getMembers().stream()
            .map(Member::getCivilization)
            .filter(c -> c != Civilization.RANDOM)
            .toList();
    List<Civilization> availableCivilizations =
        Arrays.stream(Civilization.values())
            .filter(civ -> !takenCivilizations.contains(civ))
            .collect(Collectors.toCollection(ArrayList::new));

    for (Member member : room.getMembers()) {
      if (member.getCivilization() == Civilization.RANDOM) {
        int randomIndex = random.nextInt(availableCivilizations.size());
        member.setCivilization(availableCivilizations.get(randomIndex));
        availableCivilizations.remove(randomIndex);
      }
    }
  }
}
