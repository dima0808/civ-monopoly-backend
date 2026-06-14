package me.civka.monopoly.service.impl;

import static me.civka.monopoly.util.GameUtils.getMemberFromAuthentication;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.common.PropertyType;
import me.civka.monopoly.common.Requirement;
import me.civka.monopoly.config.ConfigurationHolder;
import me.civka.monopoly.config.properties.PropertiesConfiguration;
import me.civka.monopoly.config.properties.PropertyDetails;
import me.civka.monopoly.config.properties.Upgrade;
import me.civka.monopoly.dto.member.MemberDto;
import me.civka.monopoly.dto.property.PropertyDto;
import me.civka.monopoly.dto.property.PropertyRequestDto;
import me.civka.monopoly.dto.property.PropertyRequirementsDto;
import me.civka.monopoly.dto.property.UpgradePropertyRequestDto;
import me.civka.monopoly.message.PropertyMessage;
import me.civka.monopoly.repository.MemberRepository;
import me.civka.monopoly.repository.PropertyRepository;
import me.civka.monopoly.repository.RoomRepository;
import me.civka.monopoly.repository.entity.Event;
import me.civka.monopoly.repository.entity.Event.EventType;
import me.civka.monopoly.repository.entity.Member;
import me.civka.monopoly.repository.entity.Property;
import me.civka.monopoly.repository.entity.Property.UpgradeType;
import me.civka.monopoly.repository.entity.Room;
import me.civka.monopoly.service.EventService;
import me.civka.monopoly.service.PropertyService;
import me.civka.monopoly.service.exception.property.PropertyNotFoundException;
import me.civka.monopoly.service.exception.property.RequirementNotFulfilledException;
import me.civka.monopoly.service.exception.property.UpgradeAlreadyExistsException;
import me.civka.monopoly.service.exception.room.RoomNotFoundException;
import me.civka.monopoly.service.exception.user.UserNotAllowedException;
import me.civka.monopoly.service.mapper.MemberMapper;
import me.civka.monopoly.service.mapper.PropertyMapper;
import me.civka.monopoly.util.PropertyUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PropertyServiceImpl implements PropertyService {

  private static final int MORTGAGE_START_VALUE = 5;
  private static final double MORTGAGE_RETURN_RATE = 0.4;
  private static final double MORTGAGE_BUYBACK_RATE = 0.6;

  // Government Plaza branches at level 4 into one of three departments. A cell is
  // a branching cell when its config defines LEVEL_4_1.
  private static final List<UpgradeType> GOVERNMENT_BRANCH_UPGRADES =
      List.of(UpgradeType.LEVEL_4_1, UpgradeType.LEVEL_4_2, UpgradeType.LEVEL_4_3);

  private final PropertyRepository propertyRepository;
  private final RoomRepository roomRepository;
  private final PropertyMapper propertyMapper;
  private final MemberMapper memberMapper;
  private final MemberRepository memberRepository;
  private final EventService eventService;
  private final SimpMessagingTemplate messagingTemplate;
  private final BonusService bonusService;

  private final PropertiesConfiguration propertiesConfiguration =
      ConfigurationHolder.propertiesConfiguration();

  @Override
  public Map<Integer, PropertyRequirementsDto> getRequirements() {
    Member member = getMemberFromAuthentication();
    Room room =
        roomRepository
            .getRoomByMembersContaining(member)
            .orElseThrow(() -> new RoomNotFoundException(member));

    List<Property> allProperties = propertyRepository.getPropertiesByRoom(room);
    List<Property> ownedProperties = propertyRepository.getPropertiesByMember(member);

    Map<Integer, PropertyRequirementsDto> result = new LinkedHashMap<>();

    for (Map.Entry<Integer, PropertyDetails> entry :
        propertiesConfiguration.properties().entrySet()) {
      int position = entry.getKey();
      PropertyDetails details = entry.getValue();

      Property existing =
          allProperties.stream().filter(p -> p.getPosition() == position).findFirst().orElse(null);

      if (existing != null && !existing.getMember().equalsById(member)) {
        continue;
      }

      UpgradeType nextUpgrade;
      Map<String, Boolean> requirementResults = new LinkedHashMap<>();

      if (existing == null) {
        nextUpgrade = UpgradeType.LEVEL_1;
        Upgrade upgrade = details.upgrades().get(UpgradeType.LEVEL_1);
        if (upgrade != null && upgrade.requirements() != null) {
          for (Requirement req : upgrade.requirements()) {
            requirementResults.put(req.name(), req.isBuyAllowed(member, room, ownedProperties));
          }
        }
      } else {
        UpgradeType highestOwned =
            existing.getUpgrades().stream().max(Enum::compareTo).orElse(UpgradeType.LEVEL_1);

        boolean isBranching = details.upgrades().containsKey(UpgradeType.LEVEL_4_1);
        if (isBranching && GOVERNMENT_BRANCH_UPGRADES.contains(highestOwned)) {
          // A department has already been chosen; no further upgrades are available.
          continue;
        }
        if (isBranching && highestOwned == UpgradeType.LEVEL_3) {
          Map<String, Map<String, Boolean>> branches =
              branchRequirements(existing, member, room, ownedProperties, details);
          result.put(
              position,
              new PropertyRequirementsDto(
                  UpgradeType.LEVEL_4_1.name(),
                  branches.getOrDefault(UpgradeType.LEVEL_4_1.name(), new LinkedHashMap<>()),
                  branches));
          continue;
        }

        nextUpgrade = getNextUpgrade(highestOwned);
        if (nextUpgrade == null) {
          continue;
        }
        Upgrade upgrade = details.upgrades().get(nextUpgrade);
        if (upgrade == null) {
          continue;
        }
        if (upgrade.requirements() != null) {
          for (Requirement req : upgrade.requirements()) {
            requirementResults.put(
                req.name(), req.isUpgradeAllowed(existing, member, room, ownedProperties));
          }
        }
      }

      result.put(position, new PropertyRequirementsDto(nextUpgrade.name(), requirementResults, null));
    }

    return result;
  }

  private Map<String, Map<String, Boolean>> branchRequirements(
      Property existing,
      Member member,
      Room room,
      List<Property> ownedProperties,
      PropertyDetails details) {
    Map<String, Map<String, Boolean>> branches = new LinkedHashMap<>();
    for (UpgradeType branch : GOVERNMENT_BRANCH_UPGRADES) {
      Upgrade upgrade = details.upgrades().get(branch);
      if (upgrade == null) {
        continue;
      }
      Map<String, Boolean> requirementResults = new LinkedHashMap<>();
      if (upgrade.requirements() != null) {
        for (Requirement req : upgrade.requirements()) {
          requirementResults.put(
              req.name(), req.isUpgradeAllowed(existing, member, room, ownedProperties));
        }
      }
      branches.put(branch.name(), requirementResults);
    }
    return branches;
  }

  private UpgradeType getNextUpgrade(UpgradeType current) {
    UpgradeType[] values = UpgradeType.values();
    int nextOrdinal = current.ordinal() + 1;
    if (nextOrdinal >= values.length) {
      return null;
    }
    return values[nextOrdinal];
  }

  @Override
  public List<PropertyDto> getPropertiesByRoom(UUID roomReference) {
    Room room =
        roomRepository
            .findById(roomReference)
            .orElseThrow(() -> new RoomNotFoundException(roomReference));

    return propertyMapper.toPropertyDto(propertyRepository.getPropertiesByRoom(room));
  }

  @Override
  public PropertyDto buyProperty(PropertyRequestDto buyRequest) {
    Member member = getMemberFromAuthentication();
    int position = buyRequest.getPosition();

    if (member.getPosition() != position) {
      throw new UserNotAllowedException("User is not on the property position.");
    }

    if (eventService.findByMemberAndType(member, EventType.BUY_PROPERTY) == null) {
      throw new UserNotAllowedException("No BUY_PROPERTY event for this member.");
    }

    int price = checkForPrice(member, position, UpgradeType.LEVEL_1);

    Room room =
        roomRepository
            .getRoomByMembersContaining(member)
            .orElseThrow(() -> new RoomNotFoundException(member));
    List<Property> ownedProperties = propertyRepository.getPropertiesByMember(member);

    checkForBuyRequirements(member, room, ownedProperties, position);
    checkForCurrentTurn(member, room);

    member.setGold(member.getGold() - price);

    Property property =
        Property.builder()
            .position(position)
            .mortgage(-1)
            .turnOfLastChange(room.getTurn())
            .roundOfLastChange(member.getRoundsMade())
            .upgrades(new ArrayList<>(List.of(UpgradeType.LEVEL_1)))
            .bonuses(new ArrayList<>())
            .member(memberRepository.save(member))
            .room(room)
            .build();

    propertyRepository.save(property);
    List<Property> allMemberProperties = propertyRepository.getPropertiesByMember(member);
    List<Property> bonusChanged = bonusService.recalculateBonuses(allMemberProperties, position);
    List<PropertyDto> bonusUpdateDtos =
        bonusChanged.stream().map(propertyMapper::toPropertyDto).toList();
    PropertyDto propertyDto = propertyMapper.toPropertyDto(property);
    eventService.deleteEvent(member, EventType.BUY_PROPERTY);
    sendPropertyMessage(
        room, propertyDto, bonusUpdateDtos, List.of(member),
        PropertyMessage.MessageType.PROPERTY_BUY);
    return propertyDto;
  }

  @Override
  public PropertyDto upgradeProperty(UpgradePropertyRequestDto upgradeRequest) {
    Member member = getMemberFromAuthentication();
    int position = upgradeRequest.getPosition();
    UpgradeType upgradeType = UpgradeType.valueOf(upgradeRequest.getUpgradeType());
    Property property =
        propertyRepository
            .getPropertyByPositionAndMember(position, member)
            .orElseThrow(() -> new PropertyNotFoundException(position, member));

    if (property.getUpgrades().contains(upgradeType)) {
      throw new UpgradeAlreadyExistsException(upgradeType);
    }

    if (GOVERNMENT_BRANCH_UPGRADES.contains(upgradeType)) {
      boolean ownsLevel3 = property.getUpgrades().contains(UpgradeType.LEVEL_3);
      boolean ownsDepartment =
          property.getUpgrades().stream().anyMatch(GOVERNMENT_BRANCH_UPGRADES::contains);
      if (!ownsLevel3 || ownsDepartment) {
        throw new UserNotAllowedException(
            "A Government Plaza department can only be chosen once, from level 3.");
      }
    } else if (property.getUpgrades().stream()
        .noneMatch((u) -> u.ordinal() == upgradeType.ordinal() - 1)) {
      throw new UserNotAllowedException("Upgrades must be purchased in order.");
    }

    int price = checkForPrice(member, position, upgradeType);

    Room room =
        roomRepository
            .getRoomByMembersContaining(member)
            .orElseThrow(() -> new RoomNotFoundException(member));
    List<Property> ownedProperties = propertyRepository.getPropertiesByMember(member);

    checkForUpgradeRequirements(member, property, room, ownedProperties, upgradeType);

    checkForCurrentTurn(member);

    member.setGold(member.getGold() - price);
    property.getUpgrades().add(upgradeType);
    property.setTurnOfLastChange(room.getTurn());
    property.setRoundOfLastChange(member.getRoundsMade());

    memberRepository.save(member);
    propertyRepository.save(property);
    List<Property> allMemberProperties = propertyRepository.getPropertiesByMember(member);
    List<Property> bonusChanged = bonusService.recalculateBonuses(allMemberProperties, position);
    List<PropertyDto> bonusUpdateDtos =
        bonusChanged.stream().map(propertyMapper::toPropertyDto).toList();
    PropertyDto propertyDto = propertyMapper.toPropertyDto(property);
    sendPropertyMessage(
        room, propertyDto, bonusUpdateDtos, List.of(member),
        PropertyMessage.MessageType.PROPERTY_UPGRADE);
    return propertyDto;
  }

  @Override
  public PropertyDto mortgageProperty(PropertyRequestDto mortgageRequest) {
    Member member = getMemberFromAuthentication();
    int position = mortgageRequest.getPosition();
    Property property =
        propertyRepository
            .getPropertyByPositionAndMember(position, member)
            .orElseThrow(() -> new PropertyNotFoundException(position, member));

    if (property.getMortgage() != -1) {
      throw new UserNotAllowedException("PropertyDetails is already mortgaged.");
    }

    int price =
        propertiesConfiguration
            .properties()
            .get(position)
            .upgrades()
            .get(UpgradeType.LEVEL_1)
            .price();
    Room room =
        roomRepository
            .getRoomByMembersContaining(member)
            .orElseThrow(() -> new RoomNotFoundException(member));

    member.setGold(member.getGold() + (int) (price * MORTGAGE_RETURN_RATE));
    property.setMortgage(MORTGAGE_START_VALUE);
    property.setTurnOfLastChange(room.getTurn());
    property.setRoundOfLastChange(member.getRoundsMade());

    memberRepository.save(member);
    propertyRepository.save(property);
    List<Property> allMemberProperties = propertyRepository.getPropertiesByMember(member);
    List<Property> bonusChanged = bonusService.recalculateBonuses(allMemberProperties, position);
    List<PropertyDto> bonusUpdateDtos =
        bonusChanged.stream().map(propertyMapper::toPropertyDto).toList();
    PropertyDto propertyDto = propertyMapper.toPropertyDto(property);
    sendPropertyMessage(
        room, propertyDto, bonusUpdateDtos, List.of(member),
        PropertyMessage.MessageType.PROPERTY_MORTGAGE);
    return propertyDto;
  }

  @Override
  public PropertyDto demoteUpgrade(PropertyRequestDto demoteRequest) {
    Member member = getMemberFromAuthentication();
    int position = demoteRequest.getPosition();
    Property property =
        propertyRepository
            .getPropertyByPositionAndMember(position, member)
            .orElseThrow(() -> new PropertyNotFoundException(position, member));

    if (property.getUpgrades().size() == 1) {
      throw new UserNotAllowedException("PropertyDetails has no upgrades to demote.");
    }

    UpgradeType lastUpgrade = property.getUpgrades().getLast();

    int price =
        propertiesConfiguration.properties().get(position).upgrades().get(lastUpgrade).price();
    Room room =
        roomRepository
            .getRoomByMembersContaining(member)
            .orElseThrow(() -> new RoomNotFoundException(member));

    member.setGold(member.getGold() + price);
    property.getUpgrades().remove(lastUpgrade);
    property.setTurnOfLastChange(room.getTurn());
    property.setRoundOfLastChange(member.getRoundsMade());

    memberRepository.save(member);
    propertyRepository.save(property);
    List<Property> allMemberProperties = propertyRepository.getPropertiesByMember(member);
    List<Property> bonusChanged = bonusService.recalculateBonuses(allMemberProperties, position);
    List<PropertyDto> bonusUpdateDtos =
        bonusChanged.stream().map(propertyMapper::toPropertyDto).toList();
    PropertyDto propertyDto = propertyMapper.toPropertyDto(property);
    sendPropertyMessage(
        room, propertyDto, bonusUpdateDtos, List.of(member),
        PropertyMessage.MessageType.PROPERTY_DEMOTE);
    return propertyDto;
  }

  @Override
  public PropertyDto buybackProperty(PropertyRequestDto buybackRequest) {
    Member member = getMemberFromAuthentication();
    int position = buybackRequest.getPosition();
    Property property =
        propertyRepository
            .getPropertyByPositionAndMember(position, member)
            .orElseThrow(() -> new PropertyNotFoundException(position, member));

    if (property.getMortgage() == -1) {
      throw new UserNotAllowedException("PropertyDetails is not mortgaged.");
    }

    int price =
        propertiesConfiguration
            .properties()
            .get(position)
            .upgrades()
            .get(UpgradeType.LEVEL_1)
            .price();

    int buybackPrice = (int) (price * MORTGAGE_BUYBACK_RATE);
    if (member.getGold() < buybackPrice) {
      throw new UserNotAllowedException("User does not have enough gold to buyback property.");
    }

    checkForCurrentTurn(member);

    Room room =
        roomRepository
            .getRoomByMembersContaining(member)
            .orElseThrow(() -> new RoomNotFoundException(member));

    member.setGold(member.getGold() - buybackPrice);
    property.setMortgage(-1);
    property.setTurnOfLastChange(room.getTurn());
    property.setRoundOfLastChange(member.getRoundsMade());

    memberRepository.save(member);
    propertyRepository.save(property);
    List<Property> allMemberProperties = propertyRepository.getPropertiesByMember(member);
    List<Property> bonusChanged = bonusService.recalculateBonuses(allMemberProperties, position);
    List<PropertyDto> bonusUpdateDtos =
        bonusChanged.stream().map(propertyMapper::toPropertyDto).toList();
    PropertyDto propertyDto = propertyMapper.toPropertyDto(property);
    sendPropertyMessage(
        room, propertyDto, bonusUpdateDtos, List.of(member),
        PropertyMessage.MessageType.PROPERTY_BUYBACK);
    return propertyDto;
  }

  @Override
  public PropertyDto payRent(PropertyRequestDto payRentRequest) {
    Member member = getMemberFromAuthentication();
    int position = payRentRequest.getPosition();

    if (member.getPosition() != position) {
      throw new UserNotAllowedException("User is not on the property position.");
    }

    Room room =
        roomRepository
            .getRoomByMembersContaining(member)
            .orElseThrow(() -> new RoomNotFoundException(member));

    Property property =
        propertyRepository
            .getPropertyByPositionAndRoom(position, room)
            .orElseThrow(() -> new PropertyNotFoundException(position, member));

    if (property.getMember().equalsById(member)) {
      throw new UserNotAllowedException("Cannot pay rent on own property.");
    }

    Event event = eventService.findByMemberAndType(member, EventType.FOREIGN_PROPERTY);
    if (event == null) {
      throw new UserNotAllowedException("No FOREIGN_PROPERTY event for this member.");
    }

    int rent = PropertyUtils.calculateGoldOnStep(property);

    PropertyDetails propertyDetails = propertiesConfiguration.properties().get(position);
    if (propertyDetails.type() == PropertyType.DISTRICT_ENCAMPMENT) {
      rent = rent * event.getExt().getRoll();
    }

    if (member.getGold() < rent) {
      throw new UserNotAllowedException("User does not have enough gold to pay rent.");
    }

    member.setGold(member.getGold() - rent);

    Member owner = property.getMember();
    owner.setGold(owner.getGold() + rent);

    memberRepository.save(member);
    memberRepository.save(owner);

    eventService.deleteEvent(member, EventType.FOREIGN_PROPERTY);

    PropertyDto propertyDto = propertyMapper.toPropertyDto(property);
    sendPropertyMessage(
        room, propertyDto, List.of(member, owner), PropertyMessage.MessageType.RENT_PAY);
    return propertyDto;
  }

  private void sendPropertyMessage(
      Room room,
      PropertyDto propertyDto,
      List<Member> affectedMembers,
      PropertyMessage.MessageType type) {
    sendPropertyMessage(room, propertyDto, null, affectedMembers, type);
  }

  private void sendPropertyMessage(
      Room room,
      PropertyDto propertyDto,
      List<PropertyDto> bonusUpdates,
      List<Member> affectedMembers,
      PropertyMessage.MessageType type) {
    List<MemberDto> memberDtos = affectedMembers.stream().map(memberMapper::toMemberDto).toList();
    messagingTemplate.convertAndSend(
        "/topic/games/" + room.getReference(),
        PropertyMessage.of(propertyDto, bonusUpdates, memberDtos, type));
  }

  private int checkForPrice(Member member, int position, UpgradeType upgradeType) {
    int price =
        propertiesConfiguration.properties().get(position).upgrades().get(upgradeType).price();

    if (member.getGold() < price) {
      throw new UserNotAllowedException("User does not have enough gold to buy property.");
    }
    return price;
  }

  private void checkForCurrentTurn(Member member, Room room) {
    if (!member.equalsById(room.getMembers().get(room.getTurnIndex()))) {
      throw new UserNotAllowedException("It is not user's turn.");
    }
  }

  private void checkForCurrentTurn(Member member) {
    Room room =
        roomRepository
            .getRoomByMembersContaining(member)
            .orElseThrow(() -> new RoomNotFoundException(member));

    checkForCurrentTurn(member, room);
  }

  private void checkForBuyRequirements(
      Member member, Room room, List<Property> ownedProperties, int position) {
    List<Requirement> requirements =
        propertiesConfiguration
            .properties()
            .get(position)
            .upgrades()
            .get(UpgradeType.LEVEL_1)
            .requirements();

    requirements.stream()
        .filter(requirement -> !requirement.isBuyAllowed(member, room, ownedProperties))
        .forEach(
            requirement -> {
              throw new RequirementNotFulfilledException(requirement);
            });
  }

  private void checkForUpgradeRequirements(
      Member member,
      Property property,
      Room room,
      List<Property> ownedProperties,
      UpgradeType upgradeType) {
    List<Requirement> requirements =
        propertiesConfiguration
            .properties()
            .get(property.getPosition())
            .upgrades()
            .get(upgradeType)
            .requirements();

    requirements.stream()
        .filter(
            requirement -> !requirement.isUpgradeAllowed(property, member, room, ownedProperties))
        .forEach(
            requirement -> {
              throw new RequirementNotFulfilledException(requirement);
            });
  }
}
