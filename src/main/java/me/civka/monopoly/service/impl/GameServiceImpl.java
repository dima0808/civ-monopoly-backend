package me.civka.monopoly.service.impl;

import static me.civka.monopoly.event.GlobalGameScheduler.delegateTimer;
import static me.civka.monopoly.util.GameUtils.BOARD_SIZE;
import static me.civka.monopoly.util.GameUtils.getMemberFromAuthentication;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import me.civka.monopoly.common.ScienceProject;
import me.civka.monopoly.common.VictoryType;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.common.AdditionalEffectType;
import me.civka.monopoly.config.ConfigurationHolder;
import me.civka.monopoly.config.game.ArmySpending;
import me.civka.monopoly.config.game.GameConfiguration;
import me.civka.monopoly.repository.entity.AdditionalEffect;
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
import me.civka.monopoly.repository.entity.Event;
import me.civka.monopoly.repository.entity.Event.EventType;
import me.civka.monopoly.repository.entity.EventExtraData;
import me.civka.monopoly.repository.entity.Member;
import me.civka.monopoly.repository.entity.Member.Civilization;
import me.civka.monopoly.repository.entity.Property;
import me.civka.monopoly.repository.entity.Room;
import me.civka.monopoly.service.EventService;
import me.civka.monopoly.service.GameService;
import me.civka.monopoly.service.exception.room.RoomNotFoundException;
import me.civka.monopoly.service.exception.user.UserNotAllowedException;
import me.civka.monopoly.service.mapper.RoomMapper;
import me.civka.monopoly.util.PropertyUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

  private static final int DICE_SIZE = 6;
  private static final int DEFAULT_ARMY_SPENDING_INDEX = 1;

  private final RoomRepository roomRepository;
  private final RoomMapper roomMapper;
  private final Random random = new Random();
  private final PropertyRepository propertyRepository;
  private final MemberRepository memberRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final EventService eventService;

  private final GameConfiguration gameConfiguration = ConfigurationHolder.gameConfiguration();

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
  public RoomDto endTurn(int armySpendingIndex) {
    Room room = getRoomForTurn();

    if (!room.getIsDiceRolled()) {
      throw new UserNotAllowedException("User must roll the dice before ending the turn.");
    }

    return handleEndTurn(room, false, armySpendingIndex);
  }

  @Override
  @Transactional
  public void forceRollDice(UUID roomReference) {
    Room room =
        roomRepository
            .findById(roomReference)
            .orElseThrow(() -> new RoomNotFoundException(roomReference));

    if (room.getWinner() != null) return;

    handleRollDice(room, true);
  }

  @Override
  @Transactional
  public void forceEndTurn(UUID roomReference) {
    Room room =
        roomRepository
            .findById(roomReference)
            .orElseThrow(() -> new RoomNotFoundException(roomReference));

    if (room.getWinner() != null) return;

    handleEndTurn(room, true, DEFAULT_ARMY_SPENDING_INDEX);
  }

  @Override
  @Transactional
  public RoomDto teleport(int position) {
    Room room = getRoomForTurn();

    if (!room.getIsDiceRolled()) {
      throw new UserNotAllowedException("User must roll the dice before teleporting.");
    }

    Member member = room.getMembers().get(room.getTurnIndex());
    Event teleportEvent = eventService.findByMemberAndType(member, EventType.TELEPORT);
    if (teleportEvent == null) {
      throw new UserNotAllowedException("No TELEPORT event found.");
    }

    EventExtraData ext = teleportEvent.getExt();
    if (position != ext.getTeleportOption1()
        && position != ext.getTeleportOption2()
        && position != ext.getTeleportOption3()) {
      throw new UserNotAllowedException("Invalid teleport destination.");
    }

    member.setPosition(position);
    memberRepository.save(member);

    eventService.deleteEvent(member, EventType.TELEPORT);
    eventService.handleNewPosition(member, 0, 0);

    RoomDto roomDto = roomMapper.toRoomDto(roomRepository.save(room));
    convertAndSendTo(room.getReference(), roomDto, MessageType.TELEPORT);

    return roomDto;
  }

  private RoomDto handleRollDice(Room room, boolean isForced) {
    int firstRoll = random.nextInt(DICE_SIZE) + 1;
    int secondRoll = random.nextInt(DICE_SIZE) + 1;

    Member member = room.getMembers().get(room.getTurnIndex());
    int newPosition = member.getPosition() + firstRoll + secondRoll;
    if (newPosition >= BOARD_SIZE) {
      member.setRoundsMade(member.getRoundsMade() + 1);
      member.setGold(member.getGold() + gameConfiguration.goldForPassingRound());
    }
    member.setPosition(newPosition % BOARD_SIZE);

    List<Property> ownedProperties = propertyRepository.getPropertiesByMember(member);
    int gpt = PropertyUtils.calculateGpt(ownedProperties);
    member.setGold(member.getGold() + gpt);

    member.setGold(member.getGold() + applyAdditionalEffects(member));

    if (member.getTurnsToNextScienceProject() > 0) {
      member.setTurnsToNextScienceProject(member.getTurnsToNextScienceProject() - 1);
    }

    if (member.getExpeditionTurns() != null && member.getExpeditionTurns() > 0) {
      member.setExpeditionTurns(member.getExpeditionTurns() - 1);
    }

    memberRepository.save(member);

    room.setIsDiceRolled(true);
    RoomDto roomDto = roomMapper.toRoomDto(roomRepository.save(room));
    roomDto.setExt(RoomExtraData.of(new DiceResult(firstRoll, secondRoll)));
    convertAndSendTo(
        room.getReference(),
        roomDto,
        isForced ? MessageType.FORCE_ROLL_DICE : MessageType.ROLL_DICE);

    eventService.handleNewPosition(member, firstRoll, secondRoll);

    if (member.getTurnsToNextScienceProject() != -1
        && member.getTurnsToNextScienceProject() - calculateScienceBoost(member) <= 0) {
      member.setTurnsToNextScienceProject(gameConfiguration.science().basicTurnAmount());
      memberRepository.save(member);
      eventService.addEvent(member, Event.EventType.PROJECTS_SCIENCE);
    }

    delegateTimer(room.getReference());

    return roomDto;
  }

  private RoomDto handleEndTurn(Room room, boolean isForced, int armySpendingIndex) {
    Member member = room.getMembers().get(room.getTurnIndex());

    if (!isForced) {
      if (!member.getEvents().isEmpty()) {
        throw new UserNotAllowedException("Must resolve all events before ending turn.");
      }
    } else {
      handleForceEndTurnPenalties(member);
    }

    applyArmySpending(member, armySpendingIndex);

    if (checkVictoryConditions(room, member)) {
      RoomDto roomDto = roomMapper.toRoomDto(roomRepository.save(room));
      convertAndSendTo(room.getReference(), roomDto, MessageType.GAME_OVER);
      return roomDto;
    }

    delegateNextTurn(room);

    if (room.getWinner() != null) {
      RoomDto roomDto = roomMapper.toRoomDto(roomRepository.save(room));
      convertAndSendTo(room.getReference(), roomDto, MessageType.GAME_OVER);
      return roomDto;
    }

    RoomDto roomDto = roomMapper.toRoomDto(roomRepository.save(room));
    convertAndSendTo(
        room.getReference(), roomDto, isForced ? MessageType.FORCE_END_TURN : MessageType.END_TURN);

    delegateTimer(room.getReference());

    return roomDto;
  }

  // Applies per-turn gold income from active additional effects, then ticks down
  // their remaining turns and removes the ones that just expired.
  private int applyAdditionalEffects(Member member) {
    Map<AdditionalEffectType, Integer> goldPerTurn = gameConfiguration.additionalGoldPerTurn();
    int income = 0;
    Iterator<AdditionalEffect> iterator = member.getAdditionalEffects().iterator();
    while (iterator.hasNext()) {
      AdditionalEffect effect = iterator.next();
      income += goldPerTurn.getOrDefault(effect.getType(), 0);
      if (effect.getTurnsLeft() != null && effect.getTurnsLeft() > 0) {
        effect.setTurnsLeft(effect.getTurnsLeft() - 1);
        if (effect.getTurnsLeft() == 0) {
          iterator.remove();
        }
      }
    }
    return income;
  }

  private int calculateScienceBoost(Member member) {
    var science = gameConfiguration.science();
    List<Property> owned = propertyRepository.getPropertiesByMember(member);
    List<Integer> campusPositions = PropertyUtils.getPositionByName("Campus");
    List<Integer> govPlazaPositions = PropertyUtils.getPositionByName("Government Plaza");

    int boost = 0;
    for (Property p : owned) {
      if (p.getMortgage() != -1) continue;
      int pos = p.getPosition();
      if (campusPositions.contains(pos)
          && p.getUpgrades().contains(Property.UpgradeType.LEVEL_4)) {
        boost += science.labBoost();
      }
      if (govPlazaPositions.contains(pos)
          && p.getUpgrades().contains(Property.UpgradeType.LEVEL_4_1)) {
        boost += science.governmentBoost();
      }
      if (pos == 46) {
        boost += science.oxfordBoost();
      }
      if (pos == 47) {
        boost += science.spaceportBoost();
      }
    }
    return boost;
  }

  private void applyArmySpending(Member member, int armySpendingIndex) {
    List<ArmySpending> spending = gameConfiguration.armySpending();
    if (armySpendingIndex < 0 || armySpendingIndex >= spending.size()) {
      throw new UserNotAllowedException("Invalid army spending index.");
    }
    ArmySpending selected = spending.get(armySpendingIndex);
    member.setGold(member.getGold() + selected.gold());
    member.setStrength(member.getStrength() + selected.strength());
    memberRepository.save(member);
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
        if (room.getTurn() >= gameConfiguration.maxTurns()) {
          Member topScorer = room.getMembers().stream()
              .max(Comparator.comparingInt(Member::getScore))
              .orElse(null);
          if (topScorer != null) {
            setWinner(room, topScorer, VictoryType.SCORE);
          }
        }
        convertAndSendTo(room.getReference(), roomMapper.toRoomDto(room), MessageType.NEW_TURN);
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

    if (room.getWinner() != null) {
      throw new UserNotAllowedException("Game is over.");
    }

    if (!member.equalsById(room.getMembers().get(room.getTurnIndex()))) {
      throw new UserNotAllowedException("It is not user's turn.");
    }

    return room;
  }

  private boolean checkVictoryConditions(Room room, Member member) {
    int propertyCount = propertyRepository.getPropertiesByMember(member).size();
    if (propertyCount >= gameConfiguration.militaryTarget()) {
      return setWinner(room, member, VictoryType.MILITARY);
    }

    int maxOther = room.getMembers().stream()
        .filter(m -> !m.equalsById(member))
        .mapToInt(Member::getTourism)
        .max().orElse(0);
    if (member.getTourism() >= 2 * maxOther + gameConfiguration.tourismAdditionalThreshold()) {
      return setWinner(room, member, VictoryType.CULTURE);
    }

    var finished = member.getFinishedScienceProjects();
    boolean allMilestones = finished.containsAll(
        List.of(ScienceProject.SATELLITE, ScienceProject.MOON,
                ScienceProject.MARS, ScienceProject.EXOPLANET));
    if (allMilestones && member.getExpeditionTurns() != null && member.getExpeditionTurns() == 0) {
      return setWinner(room, member, VictoryType.SCIENCE);
    }

    return false;
  }

  private boolean setWinner(Room room, Member member, VictoryType type) {
    room.setWinner(member.getUser().getUsername());
    room.setVictoryType(type);
    return true;
  }

  private void convertAndSendTo(UUID roomReference, RoomDto memberDto, MessageType type) {
    messagingTemplate.convertAndSend(
        "/topic/games/" + roomReference, GameMessage.of(memberDto, type));
  }
}
