package me.civka.monopoly.service.impl;

import static me.civka.monopoly.event.GlobalGameScheduler.delegateTimer;
import static me.civka.monopoly.util.GameUtils.BOARD_SIZE;
import static me.civka.monopoly.util.GameUtils.getMemberFromAuthentication;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.game.CivilizationListDto;
import me.civka.monopoly.dto.game.ColorListDto;
import me.civka.monopoly.dto.room.RoomDto;
import me.civka.monopoly.dto.room.ext.DiceResult;
import me.civka.monopoly.dto.room.ext.RoomExtraData;
import me.civka.monopoly.message.GameMessage;
import me.civka.monopoly.message.GameMessage.MessageType;
import me.civka.monopoly.repository.MemberRepository;
import me.civka.monopoly.repository.PropertyRepository;
import me.civka.monopoly.repository.RoomRepository;
import me.civka.monopoly.repository.entity.Event.EventType;
import me.civka.monopoly.repository.entity.Member;
import me.civka.monopoly.repository.entity.Member.Civilization;
import me.civka.monopoly.repository.entity.Property;
import me.civka.monopoly.repository.entity.Room;
import me.civka.monopoly.service.EventService;
import me.civka.monopoly.service.GameService;
import me.civka.monopoly.service.exception.room.RoomNotFoundException;
import me.civka.monopoly.service.exception.user.UserNotAllowedException;
import me.civka.monopoly.service.mapper.RoomMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

  private static final int DICE_SIZE = 6;
  private static final int MORTGAGE_PENALTY_VALUE = 5;

  private final RoomRepository roomRepository;
  private final RoomMapper roomMapper;
  private final Random random = new Random();
  private final PropertyRepository propertyRepository;
  private final MemberRepository memberRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final EventService eventService;

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

    if (room.getIsStarted()) {
      throw new UserNotAllowedException("Game has already been started.");
    }

    if (!room.isOwnedBy(member.getUser())) {
      throw new UserNotAllowedException("User are not allowed to start the properties.");
    }

    room.setIsStarted(true);
    assignRandomCivilizations(room);

    delegateNextTurn(room);

    RoomDto roomDto = roomMapper.toRoomDto(roomRepository.save(room));
    convertAndSendTo(room.getReference(), roomDto, MessageType.START);

    delegateTimer(room.getReference());

    return roomDto;
  }

  @Override
  @Transactional
  public RoomDto rollDice() {
    Room room = getRoomForTurn();

    if (room.getIsDiceRolled()) {
      throw new UserNotAllowedException("User has already rolled the dice this turn.");
    }

    return handleRollDice(room, false);
  }

  @Override
  @Transactional
  public RoomDto endTurn() {
    Room room = getRoomForTurn();

    if (!room.getIsDiceRolled()) {
      throw new UserNotAllowedException("User must roll the dice before ending the turn.");
    }

    return handleEndTurn(room, false);
  }

  @Override
  @Transactional
  public void forceRollDice(UUID roomReference) {
    Room room =
        roomRepository
            .findById(roomReference)
            .orElseThrow(() -> new RoomNotFoundException(roomReference));

    handleRollDice(room, true);
  }

  @Override
  @Transactional
  public void forceEndTurn(UUID roomReference) {
    Room room =
        roomRepository
            .findById(roomReference)
            .orElseThrow(() -> new RoomNotFoundException(roomReference));

    handleEndTurn(room, true);
  }

  private RoomDto handleRollDice(Room room, boolean isForced) {
    int firstRoll = random.nextInt(DICE_SIZE) + 1;
    int secondRoll = random.nextInt(DICE_SIZE) + 1;

    Member member = room.getMembers().get(room.getTurnIndex());
    member.setPosition((member.getPosition() + firstRoll + secondRoll) % BOARD_SIZE);
    memberRepository.save(member);

    room.setIsDiceRolled(true);
    RoomDto roomDto = roomMapper.toRoomDto(roomRepository.save(room));
    roomDto.setExt(RoomExtraData.of(new DiceResult(firstRoll, secondRoll)));
    convertAndSendTo(
        room.getReference(),
        roomDto,
        isForced ? MessageType.FORCE_ROLL_DICE : MessageType.ROLL_DICE);

    eventService.handleNewPosition(member, firstRoll, secondRoll);

    delegateTimer(room.getReference());

    return roomDto;
  }

  private RoomDto handleEndTurn(Room room, boolean isForced) {
    Member member = room.getMembers().get(room.getTurnIndex());

    if (!isForced) {
      if (!member.getEvents().isEmpty()) {
        throw new UserNotAllowedException("Must resolve all events before ending turn.");
      }
    } else {
      handleForceEndTurnPenalties(member);
    }

    delegateNextTurn(room);

    RoomDto roomDto = roomMapper.toRoomDto(roomRepository.save(room));
    convertAndSendTo(
        room.getReference(), roomDto, isForced ? MessageType.FORCE_END_TURN : MessageType.END_TURN);

    delegateTimer(room.getReference());

    return roomDto;
  }

  private void handleForceEndTurnPenalties(Member member) {
    if (member.getEvents().isEmpty()) {
      return;
    }

//    boolean hasForeignProperty =
//        member.getEvents().stream()
//            .anyMatch(event -> event.getType() == EventType.FOREIGN_PROPERTY);
//
//    if (hasForeignProperty) {
//      member.setGold(0);
//      member.setStrength(0);
//
//      List<Property> properties = propertyRepository.getPropertiesByMember(member);
//      properties.forEach(property -> property.setMortgage(MORTGAGE_PENALTY_VALUE));
//      propertyRepository.saveAll(properties);
//    }

    eventService.deleteAllEvents(member);
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

    properties.stream()
        .filter(property -> property.getMortgage() != -1)
        .forEach(property -> property.setMortgage(property.getMortgage() - 1));
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

  private Room getRoomForTurn() {
    Member member = getMemberFromAuthentication();
    Room room =
        roomRepository
            .getRoomByMembersContaining(member)
            .orElseThrow(() -> new RoomNotFoundException(member));

    if (!room.getIsStarted()) {
      throw new UserNotAllowedException("Game has not been started yet.");
    }

    if (!member.equalsById(room.getMembers().get(room.getTurnIndex()))) {
      throw new UserNotAllowedException("It is not user's turn.");
    }

    return room;
  }

  private void convertAndSendTo(UUID roomReference, RoomDto memberDto, MessageType type) {
    messagingTemplate.convertAndSend(
        "/topic/games/" + roomReference, GameMessage.of(memberDto, type));
  }
}
