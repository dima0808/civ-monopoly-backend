package me.civka.monopoly.util;

import me.civka.monopoly.repository.entity.Member;
import me.civka.monopoly.repository.entity.User;
import me.civka.monopoly.service.exception.user.UserNotInRoomException;
import org.springframework.security.core.context.SecurityContextHolder;

public class GameUtils {

  public static final int BOARD_SIZE = 48;

  public static Member getMemberFromAuthentication() {
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Member member = user.getMember();
    if (member == null) {
      throw new UserNotInRoomException(user.getUsername());
    }
    return member;
  }
}
