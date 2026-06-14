package me.civka.monopoly.message;

import java.util.List;
import me.civka.monopoly.dto.member.MemberDto;
import me.civka.monopoly.dto.property.PropertyDto;

public record PropertyMessage(
    PropertyDto property,
    List<PropertyDto> bonusUpdates,
    List<MemberDto> members,
    MessageType type) {

  public static PropertyMessage of(
      PropertyDto property, List<MemberDto> members, MessageType type) {
    return new PropertyMessage(property, null, members, type);
  }

  public static PropertyMessage of(
      PropertyDto property,
      List<PropertyDto> bonusUpdates,
      List<MemberDto> members,
      MessageType type) {
    return new PropertyMessage(property, bonusUpdates, members, type);
  }

  public enum MessageType {
    PROPERTY_BUY,
    PROPERTY_UPGRADE,
    PROPERTY_MORTGAGE,
    PROPERTY_DEMOTE,
    PROPERTY_BUYBACK,
    RENT_PAY,
  }
}
