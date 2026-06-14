package me.civka.monopoly.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.room.RoomDto;
import me.civka.monopoly.message.GameMessage;
import me.civka.monopoly.message.GameMessage.MessageType;
import me.civka.monopoly.repository.MemberRepository;
import me.civka.monopoly.repository.RoomRepository;
import me.civka.monopoly.repository.entity.Chat;
import me.civka.monopoly.repository.entity.Member;
import me.civka.monopoly.repository.entity.Room;
import me.civka.monopoly.repository.entity.User;
import me.civka.monopoly.service.CheatCommandService;
import me.civka.monopoly.service.EventService;
import me.civka.monopoly.service.mapper.RoomMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CheatCommandServiceImpl implements CheatCommandService {

  private final MemberRepository memberRepository;
  private final RoomRepository roomRepository;
  private final RoomMapper roomMapper;
  private final SimpMessagingTemplate messagingTemplate;
  private final EventService eventService;

  @Override
  public boolean processCommand(String message, User sender, Chat chat) {
    if (chat.getRoom() == null) {
      return false;
    }

    Room room =
        roomRepository.findById(chat.getRoom().getReference()).orElse(null);
    if (room == null) {
      return false;
    }

    Member member =
        room.getMembers().stream()
            .filter(m -> m.getUser().equalsById(sender))
            .findFirst()
            .orElse(null);
    if (member == null) {
      return false;
    }

    String[] parts = message.trim().split("\\s+", 2);
    String command = parts[0].toLowerCase();

    if (parts.length < 2) {
      return false;
    }

    int value;
    try {
      value = Integer.parseInt(parts[1]);
    } catch (NumberFormatException e) {
      return false;
    }

    switch (command) {
      case "/gold" -> member.setGold(member.getGold() + value);
      case "/tourism" -> member.setTourism(member.getTourism() + value);
      case "/strength" -> member.setStrength(member.getStrength() + value);
      case "/move" -> {
        member.setPosition(value);
        memberRepository.save(member);
        eventService.handleNewPosition(member, 0, 0);
      }
      default -> {
        return false;
      }
    }

    memberRepository.save(member);

    RoomDto roomDto = roomMapper.toRoomDto(roomRepository.save(room));
    messagingTemplate.convertAndSend(
        "/topic/games/" + room.getReference(), GameMessage.of(roomDto, MessageType.CHEAT));

    return true;
  }
}
