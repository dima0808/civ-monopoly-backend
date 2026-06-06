package me.civka.monopoly.service.impl;

import static me.civka.monopoly.util.GameUtils.getMemberFromAuthentication;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.common.AdditionalEffectType;
import me.civka.monopoly.common.ProjectType;
import me.civka.monopoly.common.ScienceProject;
import me.civka.monopoly.config.ConfigurationHolder;
import me.civka.monopoly.config.game.Concert;
import me.civka.monopoly.config.game.GameConfiguration;
import me.civka.monopoly.config.game.Science;
import me.civka.monopoly.dto.member.AdditionalEffectDto;
import me.civka.monopoly.dto.member.MemberDto;
import me.civka.monopoly.dto.project.ProjectChoiceRequestDto;
import me.civka.monopoly.message.ProjectMessage;
import me.civka.monopoly.repository.AdditionalEffectRepository;
import me.civka.monopoly.repository.MemberRepository;
import me.civka.monopoly.repository.PropertyRepository;
import me.civka.monopoly.repository.RoomRepository;
import me.civka.monopoly.repository.entity.AdditionalEffect;
import me.civka.monopoly.repository.entity.Event.EventType;
import me.civka.monopoly.repository.entity.Member;
import me.civka.monopoly.repository.entity.Property;
import me.civka.monopoly.repository.entity.Property.UpgradeType;
import me.civka.monopoly.repository.entity.Room;
import me.civka.monopoly.service.EventService;
import me.civka.monopoly.service.ProjectService;
import me.civka.monopoly.service.exception.room.RoomNotFoundException;
import me.civka.monopoly.service.exception.user.UserNotAllowedException;
import me.civka.monopoly.service.mapper.MemberMapper;
import me.civka.monopoly.util.PropertyUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

  private static final int COMMERCIAL_HUB_INVESTMENT_DURATION = 10;

  // District property names backing each district-enhancement project.
  private static final String COMMERCIAL_HUB = "Commercial Hub";
  private static final String ENCAMPMENT = "Encampment";
  private static final String HARBOR = "Harbor";
  private static final String THEATER_SQUARE = "Theater Square";
  private static final String CAMPUS = "Campus";
  private static final String SPACEPORT = "Spaceport";

  private final MemberRepository memberRepository;
  private final PropertyRepository propertyRepository;
  private final RoomRepository roomRepository;
  private final AdditionalEffectRepository additionalEffectRepository;
  private final EventService eventService;
  private final MemberMapper memberMapper;
  private final SimpMessagingTemplate messagingTemplate;

  private final Random random = new Random();
  private final GameConfiguration gameConfiguration = ConfigurationHolder.gameConfiguration();

  @Override
  public MemberDto chooseProject(ProjectChoiceRequestDto request) {
    Member member = getMemberFromAuthentication();
    ProjectType projectType = parseProjectType(request.getProjectType());

    Room room = getRoom(member);
    checkForCurrentTurn(member, room);
    requireEvent(member, EventType.PROJECTS_EDGE);

    switch (projectType) {
      case HARBOR_SHIPPING ->
          member.setGold(member.getGold() + districtAmount(member, projectType, HARBOR));
      case ENCAMPMENT_TRAINING ->
          member.setStrength(member.getStrength() + districtAmount(member, projectType, ENCAMPMENT));
      case THEATER_SQUARE_PERFORMANCES ->
          member.setTourism(
              member.getTourism() + districtAmount(member, projectType, THEATER_SQUARE));
      case COMMERCIAL_HUB_INVESTMENT -> applyCommercialHubInvestment(member);
      case CAMPUS_RESEARCH_GRANTS -> grantCampusResearch(member);
      // TODO: BREAD_AND_CIRCUSES (opponent mortgage pressure) and
      // INDUSTRIAL_ZONE_LOGISTICS (wonder discount) depend on systems not yet
      // ported; space launches advance through doScienceProject. See PROJECTS_PLAN.md.
      default ->
          throw new UserNotAllowedException("Project " + projectType + " is not supported yet.");
    }

    memberRepository.save(member);
    eventService.deleteEvent(member, EventType.PROJECTS_EDGE);
    return broadcast(room, member, ProjectMessage.MessageType.PROJECT_CHOICE);
  }

  @Override
  public MemberDto doScienceProject() {
    Member member = getMemberFromAuthentication();
    Room room = getRoom(member);
    checkForCurrentTurn(member, room);
    requireEvent(member, EventType.PROJECTS_SCIENCE);

    if (!canDoScience(member)) {
      throw new UserNotAllowedException(
          "Member cannot perform a science project (needs research grants and a spaceport or a"
              + " level 4 campus).");
    }

    ScienceProject next = nextScienceProject(member);
    if (next == null || next == ScienceProject.CAMPUS) {
      throw new UserNotAllowedException("No science project available.");
    }

    Science science = gameConfiguration.science();
    if (member.getGold() < science.cost()) {
      throw new UserNotAllowedException("Member does not have enough gold for a science project.");
    }
    member.setGold(member.getGold() - science.cost());

    member.getFinishedScienceProjects().add(next);
    if (next == ScienceProject.EXOPLANET) {
      member.setExpeditionTurns(science.expeditionTurnAmount());
    } else if (next == ScienceProject.LASER) {
      member.setExpeditionTurns(Math.max(member.getExpeditionTurns() - science.laserBoost(), 0));
    }
    member.setTurnsToNextScienceProject(science.basicTurnAmount());

    memberRepository.save(member);
    eventService.deleteEvent(member, EventType.PROJECTS_SCIENCE);
    return broadcast(room, member, ProjectMessage.MessageType.SCIENCE_PROJECT);
  }

  @Override
  public MemberDto doConcert() {
    Member member = getMemberFromAuthentication();
    Room room = getRoom(member);
    checkForCurrentTurn(member, room);
    requireEvent(member, EventType.PROJECTS_CULTURE);

    Concert concert = gameConfiguration.concert();
    if (member.getGold() < concert.cost()) {
      throw new UserNotAllowedException("Member does not have enough gold for a concert.");
    }
    member.setGold(member.getGold() - concert.cost());

    int range = concert.tourismUpperBound() - concert.tourismLowerBound();
    int tourism = concert.tourismLowerBound() + (range > 0 ? random.nextInt(range) : 0);
    member.setTourism(member.getTourism() + tourism);

    memberRepository.save(member);
    eventService.deleteEvent(member, EventType.PROJECTS_CULTURE);
    return broadcast(room, member, ProjectMessage.MessageType.CONCERT);
  }

  @Override
  public List<AdditionalEffectDto> getMyAdditionalEffects() {
    Member member = getMemberFromAuthentication();
    return member.getAdditionalEffects().stream().map(this::toDto).toList();
  }

  private void applyCommercialHubInvestment(Member member) {
    int level = districtLevelNumber(member, COMMERCIAL_HUB);
    AdditionalEffectType type = AdditionalEffectType.valueOf("COMMERCIAL_HUB_INVESTMENT_" + level);
    AdditionalEffect effect =
        AdditionalEffect.builder()
            .type(type)
            .turnsLeft(COMMERCIAL_HUB_INVESTMENT_DURATION)
            .member(member)
            .build();
    additionalEffectRepository.save(effect);
    member.getAdditionalEffects().add(effect);
  }

  private void grantCampusResearch(Member member) {
    if (member.getFinishedScienceProjects().contains(ScienceProject.CAMPUS)) {
      throw new UserNotAllowedException("Research grants already obtained.");
    }
    member.getFinishedScienceProjects().add(ScienceProject.CAMPUS);
  }

  private boolean canDoScience(Member member) {
    if (!member.getFinishedScienceProjects().contains(ScienceProject.CAMPUS)) {
      return false;
    }
    List<Property> owned = propertyRepository.getPropertiesByMember(member);
    List<Integer> spaceportPositions = PropertyUtils.getPositionByName(SPACEPORT);
    List<Integer> campusPositions = PropertyUtils.getPositionByName(CAMPUS);
    boolean ownsSpaceport =
        owned.stream().anyMatch(p -> spaceportPositions.contains(p.getPosition()));
    boolean ownsCampusL4 =
        owned.stream()
            .anyMatch(
                p ->
                    campusPositions.contains(p.getPosition())
                        && p.getUpgrades().contains(UpgradeType.LEVEL_4));
    return ownsSpaceport || ownsCampusL4;
  }

  private ScienceProject nextScienceProject(Member member) {
    for (ScienceProject sp : ScienceProject.values()) {
      if (!member.getFinishedScienceProjects().contains(sp)) {
        return sp;
      }
    }
    return null;
  }

  private int districtAmount(Member member, ProjectType projectType, String districtName) {
    int level = districtLevelNumber(member, districtName);
    Integer amount = gameConfiguration.projectEffects().get(projectType).get("LEVEL_" + level);
    if (amount == null) {
      throw new UserNotAllowedException(
          "No effect configured for " + projectType + " level " + level);
    }
    return amount;
  }

  private int districtLevelNumber(Member member, String districtName) {
    List<Integer> positions = PropertyUtils.getPositionByName(districtName);
    int level =
        propertyRepository.getPropertiesByMember(member).stream()
            .filter(p -> positions.contains(p.getPosition()))
            .flatMap(p -> p.getUpgrades().stream())
            .filter(u -> u.ordinal() <= UpgradeType.LEVEL_4.ordinal())
            .mapToInt(u -> u.ordinal() + 1) // LEVEL_1 -> 1 ... LEVEL_4 -> 4
            .max()
            .orElse(0);
    if (level == 0) {
      throw new UserNotAllowedException(
          "Member does not own a " + districtName + " for this project.");
    }
    return level;
  }

  private AdditionalEffectDto toDto(AdditionalEffect effect) {
    Integer goldPerTurn =
        gameConfiguration.additionalGoldPerTurn().getOrDefault(effect.getType(), 0);
    return AdditionalEffectDto.builder()
        .type(effect.getType().name())
        .turnsLeft(effect.getTurnsLeft())
        .goldPerTurn(goldPerTurn)
        .build();
  }

  private ProjectType parseProjectType(String value) {
    try {
      return ProjectType.valueOf(value);
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new UserNotAllowedException("Unknown project type: " + value);
    }
  }

  private Room getRoom(Member member) {
    return roomRepository
        .getRoomByMembersContaining(member)
        .orElseThrow(() -> new RoomNotFoundException(member));
  }

  private void checkForCurrentTurn(Member member, Room room) {
    if (!member.equalsById(room.getMembers().get(room.getTurnIndex()))) {
      throw new UserNotAllowedException("It is not user's turn.");
    }
  }

  private void requireEvent(Member member, EventType type) {
    if (eventService.findByMemberAndType(member, type) == null) {
      throw new UserNotAllowedException("No " + type + " event for this member.");
    }
  }

  private MemberDto broadcast(Room room, Member member, ProjectMessage.MessageType type) {
    MemberDto memberDto = memberMapper.toMemberDto(member);
    messagingTemplate.convertAndSend(
        "/topic/games/" + room.getReference(), ProjectMessage.of(List.of(memberDto), type));
    return memberDto;
  }
}
