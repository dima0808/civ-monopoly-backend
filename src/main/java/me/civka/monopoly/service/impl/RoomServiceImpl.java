package me.civka.monopoly.service.impl;

import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.room.RoomCreateRequestDto;
import me.civka.monopoly.dto.room.RoomDto;
import me.civka.monopoly.dto.room.RoomListDto;
import me.civka.monopoly.message.NotificationMessage;
import me.civka.monopoly.message.NotificationMessage.NotificationType;
import me.civka.monopoly.message.RoomMessage;
import me.civka.monopoly.message.RoomMessage.MessageType;
import me.civka.monopoly.repository.ChatRepository;
import me.civka.monopoly.repository.MemberRepository;
import me.civka.monopoly.repository.RoomRepository;
import me.civka.monopoly.repository.UserRepository;
import me.civka.monopoly.repository.entity.Chat;
import me.civka.monopoly.repository.entity.Member;
import me.civka.monopoly.repository.entity.Member.Civilization;
import me.civka.monopoly.repository.entity.Member.Color;
import me.civka.monopoly.repository.entity.Room;
import me.civka.monopoly.repository.entity.User;
import me.civka.monopoly.service.RoomService;
import me.civka.monopoly.service.exception.chat.ChatNotFoundException;
import me.civka.monopoly.service.exception.member.MemberNotFoundException;
import me.civka.monopoly.service.exception.member.MemberNotInRoomException;
import me.civka.monopoly.service.exception.room.IllegalMemberLimitException;
import me.civka.monopoly.service.exception.room.InvalidRoomPasswordException;
import me.civka.monopoly.service.exception.room.RoomIsFullException;
import me.civka.monopoly.service.exception.room.RoomNotFoundException;
import me.civka.monopoly.service.exception.user.UserNotAllowedException;
import me.civka.monopoly.service.exception.user.UserNotInRoomException;
import me.civka.monopoly.service.mapper.RoomMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

  private static final int MAX_MEMBER_LIMIT = 6;

  private static final int GOLD_START_VALUE = 5000;
  private static final int STRENGTH_START_VALUE = 40;
  private static final int TOURISM_START_VALUE = 0;
  private static final int SCORE_START_VALUE = 0;

  private final RoomRepository roomRepository;
  private final RoomMapper roomMapper;
  private final UserRepository userRepository;
  private final MemberRepository memberRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final ChatRepository chatRepository;

  @Override
  public RoomListDto getAllRooms() {
    return roomMapper.toRoomListDto(roomRepository.findAll());
  }

  @Override
  public RoomDto getRoomByReference(UUID roomReference) {
    Room room =
        roomRepository
            .findById(roomReference)
            .orElseThrow(() -> new RoomNotFoundException(roomReference));
    room.setChat(
        chatRepository
            .findChatByRoomReference(roomReference)
            .orElseThrow(() -> new ChatNotFoundException(roomReference)));
    return roomMapper.toRoomDto(room);
  }

  @Override
  public RoomDto createRoom(RoomCreateRequestDto roomCreateRequestDto) {
    if (roomCreateRequestDto.getMemberLimit() > MAX_MEMBER_LIMIT) {
      throw new IllegalMemberLimitException(
          roomCreateRequestDto.getMemberLimit(), MAX_MEMBER_LIMIT);
    }
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (user.getMember() != null) {
      leaveRoom();
    }

    Room room = roomMapper.toRoomEntity(roomCreateRequestDto);
    room.setChat(Chat.builder().reference(room.getReference()).room(room).build());
    roomRepository.save(room);

    assignUserToRoom(user, room, true);

    RoomDto roomDto = roomMapper.toRoomDto(room);

    convertAndSendTo("/topic/rooms", roomDto, MessageType.CREATE);

    return roomDto;
  }

  @Override
  public RoomDto joinRoom(UUID roomReference, String password) {
    Room room =
        roomRepository
            .findById(roomReference)
            .orElseThrow(() -> new RoomNotFoundException(roomReference));
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    if (room.getIsStarted()) {
      throw new UserNotAllowedException("You cannot join a room that has already started");
    }
    if (room.getMembers().size() >= room.getMemberLimit()) {
      throw new RoomIsFullException(room.getName());
    }
    if (room.getPassword() != null && !room.getPassword().equals(password)) {
      throw new InvalidRoomPasswordException(room.getName());
    }

    if (user.getMember() != null) {
      Room currentRoom =
          roomRepository
              .getRoomByMembersContaining(user.getMember())
              .orElseThrow(() -> new RoomNotFoundException(user.getMember()));
      if (currentRoom.equalsById(room)) {
        throw new UserNotAllowedException("You are already in this room");
      }
      leaveRoom();
    }

    assignUserToRoom(user, room, false);

    RoomDto roomDto = roomMapper.toRoomDto(room);

    convertAndSendTo(
        List.of("/topic/rooms", "/topic/rooms/" + roomReference), roomDto, MessageType.JOIN);

    room.setChat(
        chatRepository
            .findChatByRoomReference(roomReference)
            .orElseThrow(() -> new ChatNotFoundException(roomReference)));

    return roomDto;
  }

  @Override
  public RoomDto leaveRoom() {
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (user.getMember() == null) {
      throw new UserNotInRoomException(user.getUsername());
    }
    Room room =
        roomRepository
            .getRoomByMembersContaining(user.getMember())
            .orElseThrow(() -> new RoomNotFoundException(user.getMember()));

    //    if (room.getIsStarted()) {
    //      throw new UserNotAllowedException("You cannot leave a room that has already started");
    //    } TODO: return back on prod

    boolean isOwnerLeaving = room.isOwnedBy(user);
    unassignUserFromRoom(user, room);

    RoomDto roomDto = roomMapper.toRoomDto(room);

    if (room.getMembers().isEmpty()) {
      roomRepository.deleteById(room.getReference());
      convertAndSendTo("/topic/rooms", roomDto, MessageType.DELETE);
      return null;
    }

    convertAndSendTo(
        List.of("/topic/rooms", "/topic/rooms/" + room.getReference()), roomDto, MessageType.LEAVE);
    if (isOwnerLeaving) {
      notifyUser(
          room.getMembers().getFirst().getUser().getUsername(), NotificationType.USER_BECAME_OWNER);
    }

    return roomDto;
  }

  @Override
  public RoomDto kickMember(UUID memberReference) {
    User owner = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Room room =
        roomRepository
            .getRoomByMembersContaining(owner.getMember())
            .orElseThrow(() -> new RoomNotFoundException(owner.getMember()));
    Member memberToKick =
        memberRepository
            .findById(memberReference)
            .orElseThrow(() -> new MemberNotFoundException(memberReference));

    if (room.getIsStarted()) {
      throw new UserNotAllowedException("Cannot kick users from a room that has already started");
    }
    if (!room.isOwnedBy(owner)) {
      throw new UserNotAllowedException("Only room owner can kick users");
    }
    if (memberToKick.getUser().equalsById(owner)) {
      throw new UserNotAllowedException("You cannot kick yourself from the room");
    }

    Room memberToKickRoom =
        roomRepository
            .getRoomByMembersContaining(memberToKick)
            .orElseThrow(() -> new RoomNotFoundException(memberToKick));
    if (!memberToKickRoom.equalsById(room)) {
      throw new MemberNotInRoomException(room.getName(), memberReference);
    }

    unassignUserFromRoom(memberToKick.getUser(), room);

    RoomDto roomDto = roomMapper.toRoomDto(room);

    convertAndSendTo(
        List.of("/topic/rooms", "/topic/rooms/" + room.getReference()), roomDto, MessageType.KICK);
    notifyUser(memberToKick.getUser().getUsername(), NotificationType.USER_WAS_KICKED);

    return roomDto;
  }

  @Override
  @Transactional
  public void deleteRoom() {
    User owner = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Room room =
        roomRepository
            .getRoomByMembersContaining(owner.getMember())
            .orElseThrow(() -> new RoomNotFoundException(owner.getMember()));

    if (room.getIsStarted()) {
      throw new UserNotAllowedException("Cannot delete a room that has already started");
    }
    if (!room.isOwnedBy(owner)) {
      throw new UserNotAllowedException("Only room owner can kick users");
    }

    List<User> users =
        room.getMembers().stream().map(Member::getUser).peek(user -> user.setMember(null)).toList();

    userRepository.saveAll(users);
    roomRepository.deleteById(room.getReference());

    convertAndSendTo("/topic/rooms", roomMapper.toRoomDto(room), MessageType.DELETE);
  }

  private void assignUserToRoom(User user, Room room, boolean isCreator) {
    Member member =
        Member.builder()
            .color(isCreator ? Color.RED : assignColor(room.getMembers()))
            .civilization(Civilization.RANDOM)
            .position(0)
            .roundsMade(0)
            .gold(GOLD_START_VALUE)
            .strength(STRENGTH_START_VALUE)
            .tourism(TOURISM_START_VALUE)
            .score(SCORE_START_VALUE)
            .joinedAt(OffsetDateTime.now())
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

  private void convertAndSendTo(String destination, RoomDto roomDto, MessageType type) {
    messagingTemplate.convertAndSend(destination, RoomMessage.of(roomDto, type));
  }

  private void notifyUser(String username, NotificationType notificationType) {
    messagingTemplate.convertAndSendToUser(
        username, "/notifications", NotificationMessage.of(notificationType));
  }

  private void convertAndSendTo(List<String> destinations, RoomDto roomDto, MessageType type) {
    destinations.forEach(destination -> convertAndSendTo(destination, roomDto, type));
  }
}
