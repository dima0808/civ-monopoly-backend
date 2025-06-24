package me.civka.monopoly.service;

import java.util.UUID;
import me.civka.monopoly.dto.room.RoomCreateRequestDto;
import me.civka.monopoly.dto.room.RoomDto;
import me.civka.monopoly.dto.room.RoomListDto;

public interface RoomService {

  RoomListDto getAllRooms();

  RoomDto getRoomByReference(UUID roomReference);

  RoomDto createRoom(RoomCreateRequestDto roomCreateRequestDto);

  RoomDto joinRoom(UUID roomReference, String password);

  RoomDto leaveRoom();

  RoomDto kickUser(UUID userReference);

  @Deprecated
  void deleteRoom();
}
