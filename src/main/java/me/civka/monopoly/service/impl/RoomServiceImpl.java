package me.civka.monopoly.service.impl;

import jakarta.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.room.RoomCreateRequestDto;
import me.civka.monopoly.dto.room.RoomDto;
import me.civka.monopoly.dto.room.RoomListDto;
import me.civka.monopoly.message.RoomMessage;
import me.civka.monopoly.message.RoomMessage.MessageType;
import me.civka.monopoly.repository.MemberRepository;
import me.civka.monopoly.repository.RoomRepository;
import me.civka.monopoly.repository.UserRepository;
import me.civka.monopoly.repository.entity.Member;
import me.civka.monopoly.repository.entity.Member.Civilization;
import me.civka.monopoly.repository.entity.Member.Color;
import me.civka.monopoly.repository.entity.Room;
import me.civka.monopoly.repository.entity.User;
import me.civka.monopoly.service.RoomService;
import me.civka.monopoly.service.exception.IllegalMemberLimitException;
import me.civka.monopoly.service.exception.InvalidRoomPasswordException;
import me.civka.monopoly.service.exception.MemberNotFoundException;
import me.civka.monopoly.service.exception.MemberNotInRoomException;
import me.civka.monopoly.service.exception.RoomIsFullException;
import me.civka.monopoly.service.exception.RoomNotFoundException;
import me.civka.monopoly.service.exception.UserAlreadyInRoomException;
import me.civka.monopoly.service.exception.UserNotAllowedException;
import me.civka.monopoly.service.mapper.RoomMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

  private static final int MAX_MEMBER_LIMIT = 6;

  private static final int GOLD_START_VALUE = 5000;
  private static final int STRENGTH_START_VALUE = 0;
  private static final int TOURISM_START_VALUE = 0;
  private static final int SCORE_START_VALUE = 0;

  private final RoomRepository roomRepository;
  private final RoomMapper roomMapper;
  private final UserRepository userRepository;
  private final MemberRepository memberRepository;
  private final SimpMessagingTemplate messagingTemplate;

  @Override
  public RoomListDto getAllRooms() {
    return roomMapper.toRoomListDto(roomRepository.findAll());
  }

  @Override
  public RoomDto getRoomByReference(UUID roomReference) {
    return roomMapper.toRoomDto(
        roomRepository
            .findById(roomReference)
            .orElseThrow(() -> new RoomNotFoundException(roomReference)));
  }

  @Override
  @Transactional
  public RoomDto createRoom(RoomCreateRequestDto roomCreateRequestDto) {
    if (roomCreateRequestDto.getMemberLimit() > MAX_MEMBER_LIMIT) {
      throw new IllegalMemberLimitException(
          roomCreateRequestDto.getMemberLimit(), MAX_MEMBER_LIMIT);
    }
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (user.getMember() != null) {
      throw new UserAlreadyInRoomException(user.getUserReference());
    }
    Room room = roomRepository.save(roomMapper.toRoomEntity(roomCreateRequestDto));

    assignUserToRoom(user, room, true);

    convertAndSendTo("/topic/rooms", room, MessageType.CREATE);

    return roomMapper.toRoomDto(room);
  }

  @Override
  public RoomDto joinRoom(UUID roomReference, String password) {
    Room room =
        roomRepository
            .findById(roomReference)
            .orElseThrow(() -> new RoomNotFoundException(roomReference));
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    if (room.getMembers().size() >= room.getMemberLimit()) {
      throw new RoomIsFullException(room.getName());
    }
    if (room.getPassword() != null && !room.getPassword().equals(password)) {
      throw new InvalidRoomPasswordException(room.getName());
    }

    assignUserToRoom(user, room, false);

    convertAndSendTo(
        List.of("/topic/rooms", "/topic/rooms/" + roomReference), room, MessageType.JOIN);

    return roomMapper.toRoomDto(room);
  }

  @Override
  public RoomDto leaveRoom() {
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    UUID roomReference = user.getMember().getRoom().getRoomReference();
    Room room =
        roomRepository
            .findById(roomReference)
            .orElseThrow(() -> new RoomNotFoundException(roomReference));

    unassignUserFromRoom(user, room);

    if (room.getMembers().isEmpty()) {
      roomRepository.deleteById(roomReference);

      convertAndSendTo("/topic/rooms", room, MessageType.DELETE);

      return null;
    }

    convertAndSendTo(
        List.of("/topic/rooms", "/topic/rooms/" + roomReference), room, MessageType.LEAVE);

    return roomMapper.toRoomDto(room);
  }

  @Override
  public RoomDto kickMember(UUID memberReference) {
    User owner = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    UUID roomReference = owner.getMember().getRoom().getRoomReference();
    Room room =
        roomRepository
            .findById(roomReference)
            .orElseThrow(() -> new RoomNotFoundException(roomReference));
    Member memberToKick =
        memberRepository
            .findById(memberReference)
            .orElseThrow(() -> new MemberNotFoundException(memberReference));

    if (!room.getMembers().getFirst().getUser().equalsById(owner)) {
      throw new UserNotAllowedException("Only room owner can kick users");
    }
    if (memberToKick.getRoom().equalsById(room)) {
      throw new MemberNotInRoomException(room.getName(), memberReference);
    }

    unassignUserFromRoom(memberToKick.getUser(), room);

    convertAndSendTo(
        List.of("/topic/rooms", "/topic/rooms/" + roomReference), room, MessageType.KICK);

    return roomMapper.toRoomDto(room);
  }

  @Override
  public void deleteRoom() {
    User owner = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    UUID roomReference = owner.getMember().getRoom().getRoomReference();
    Room room =
        roomRepository
            .findById(roomReference)
            .orElseThrow(() -> new RoomNotFoundException(roomReference));

    if (!room.getMembers().getFirst().getUser().equalsById(owner)) {
      throw new UserNotAllowedException("Only room owner can kick users");
    }

    List<User> users =
        room.getMembers().stream().map(Member::getUser).peek(user -> user.setMember(null)).toList();

    userRepository.saveAll(users);
    roomRepository.deleteById(roomReference);

    convertAndSendTo("/topic/rooms", room, MessageType.DELETE);
  }

  private void assignUserToRoom(User user, Room room, boolean isCreator) {
    Member member =
        Member.builder()
            .color(isCreator ? Color.RED : assignColor(room.getMembers()))
            .civilization(Civilization.RANDOM)
            .gold(GOLD_START_VALUE)
            .strength(STRENGTH_START_VALUE)
            .tourism(TOURISM_START_VALUE)
            .score(SCORE_START_VALUE)
            .user(user)
            .room(room)
            .build();

    user.setMember(member);
    memberRepository.save(member);
    userRepository.save(user);
    room.getMembers().add(member); // for display purposes
  }

  private void unassignUserFromRoom(User user, Room room) {
    user.setMember(null);
    userRepository.save(user);
    room.getMembers().removeIf(m -> m.getUser().equalsById(user)); // for display purposes
  }

  private Color assignColor(List<Member> members) {
    Set<Color> unavailableColors =
        members.stream().map(Member::getColor).collect(Collectors.toSet());
    return Arrays.stream(Color.values())
        .filter((color) -> !unavailableColors.contains(color))
        .findFirst()
        .orElse(Color.BLUE);
  }

  private void convertAndSendTo(String destination, Room room, MessageType type) {
    messagingTemplate.convertAndSend(
        destination, RoomMessage.builder().room(roomMapper.toRoomDto(room)).type(type).build());
  }

  private void convertAndSendTo(List<String> destinations, Room room, MessageType type) {
    destinations.forEach(destination -> convertAndSendTo(destination, room, type));
  }
}
