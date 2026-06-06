package me.civka.monopoly.message;

import java.util.List;
import me.civka.monopoly.dto.member.MemberDto;

public record ProjectMessage(List<MemberDto> members, MessageType type) {

  public static ProjectMessage of(List<MemberDto> members, MessageType type) {
    return new ProjectMessage(members, type);
  }

  public enum MessageType {
    PROJECT_CHOICE,
    SCIENCE_PROJECT,
    CONCERT,
  }
}
