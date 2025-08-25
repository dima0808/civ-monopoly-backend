package me.civka.monopoly.service.impl;

import static me.civka.monopoly.util.GameUtils.getMemberFromAuthentication;

import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.common.Requirement;
import me.civka.monopoly.config.game.PropertiesConfiguration;
import me.civka.monopoly.dto.property.PropertyDto;
import me.civka.monopoly.dto.property.PropertyRequestDto;
import me.civka.monopoly.dto.property.UpgradePropertyRequestDto;
import me.civka.monopoly.repository.MemberRepository;
import me.civka.monopoly.repository.PropertyRepository;
import me.civka.monopoly.repository.RoomRepository;
import me.civka.monopoly.repository.entity.Member;
import me.civka.monopoly.repository.entity.Property;
import me.civka.monopoly.repository.entity.Property.UpgradeType;
import me.civka.monopoly.repository.entity.Room;
import me.civka.monopoly.service.PropertyService;
import me.civka.monopoly.service.exception.property.PropertyNotFoundException;
import me.civka.monopoly.service.exception.property.RequirementNotFulfilledException;
import me.civka.monopoly.service.exception.property.UpgradeAlreadyExistsException;
import me.civka.monopoly.service.exception.room.RoomNotFoundException;
import me.civka.monopoly.service.exception.user.UserNotAllowedException;
import me.civka.monopoly.service.mapper.PropertyMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PropertyServiceImpl implements PropertyService {

  private static final int MORTGAGE_START_VALUE = 5;
  private static final double MORTGAGE_RETURN_RATE = 0.4;
  private static final double MORTGAGE_BUYBACK_RATE = 0.6;

  private final PropertyRepository propertyRepository;
  private final RoomRepository roomRepository;
  private final PropertyMapper propertyMapper;
  private final MemberRepository memberRepository;
  private final PropertiesConfiguration propertiesConfiguration;

  @Override
  public PropertyDto buyProperty(PropertyRequestDto buyRequest) {
    Member member = getMemberFromAuthentication();
    int position = buyRequest.getPosition();

    if (member.getPosition() != position) {
      throw new UserNotAllowedException("User is not on the property position.");
    }

    int price = checkForPrice(member, position, UpgradeType.LEVEL_1);
    checkForBuyRequirements(member, position);

    Room room =
        roomRepository
            .getRoomByMembersContaining(member)
            .orElseThrow(() -> new RoomNotFoundException(member));

    checkForCurrentTurn(member, room);

    member.setGold(member.getGold() - price);

    Property property =
        Property.builder()
            .position(position)
            .mortgage(-1)
            .turnOfLastChange(room.getTurn())
            .roundOfLastChange(member.getRoundsMade())
            .upgrades(List.of(UpgradeType.LEVEL_1))
            .member(memberRepository.save(member))
            .room(room)
            .build();

    return propertyMapper.toPropertyDto(propertyRepository.save(property));
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

    if (property.getUpgrades().stream()
        .noneMatch((u) -> u.ordinal() == upgradeType.ordinal() - 1)) { // TODO: add gov plaza check
      throw new UserNotAllowedException("Upgrades must be purchased in order.");
    }

    int price = checkForPrice(member, position, upgradeType);
    checkForUpgradeRequirements(member, property, upgradeType);

    checkForCurrentTurn(member);

    member.setGold(member.getGold() - price);
    property.getUpgrades().add(upgradeType);

    memberRepository.save(member);
    return propertyMapper.toPropertyDto(propertyRepository.save(property));
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
      throw new UserNotAllowedException("Property is already mortgaged.");
    }

    int price =
        propertiesConfiguration
            .properties()
            .get(position)
            .upgrades()
            .get(UpgradeType.LEVEL_1)
            .price();
    member.setGold(member.getGold() + (int) (price * MORTGAGE_RETURN_RATE));
    property.setMortgage(MORTGAGE_START_VALUE);

    memberRepository.save(member);
    return propertyMapper.toPropertyDto(propertyRepository.save(property));
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
      throw new UserNotAllowedException("Property has no upgrades to demote.");
    }

    UpgradeType lastUpgrade = property.getUpgrades().getLast();

    int price =
        propertiesConfiguration.properties().get(position).upgrades().get(lastUpgrade).price();
    member.setGold(member.getGold() + price);
    property.getUpgrades().remove(lastUpgrade);

    memberRepository.save(member);
    return propertyMapper.toPropertyDto(propertyRepository.save(property));
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
      throw new UserNotAllowedException("Property is not mortgaged.");
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

    member.setGold(member.getGold() - buybackPrice);
    property.setMortgage(-1);

    memberRepository.save(member);
    return propertyMapper.toPropertyDto(propertyRepository.save(property));
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

  private void checkForBuyRequirements(Member member, int position) {
    List<Requirement> requirements =
        propertiesConfiguration
            .properties()
            .get(position)
            .upgrades()
            .get(UpgradeType.LEVEL_1)
            .requirements();

    requirements.stream()
        .filter(requirement -> !requirement.isBuyAllowed(member))
        .forEach(
            requirement -> {
              throw new RequirementNotFulfilledException(requirement);
            });
  }

  private void checkForUpgradeRequirements(
      Member member, Property property, UpgradeType upgradeType) {
    List<Requirement> requirements =
        propertiesConfiguration
            .properties()
            .get(property.getPosition())
            .upgrades()
            .get(upgradeType)
            .requirements();

    requirements.stream()
        .filter(requirement -> !requirement.isUpgradeAllowed(property, member))
        .forEach(
            requirement -> {
              throw new RequirementNotFulfilledException(requirement);
            });
  }
}
