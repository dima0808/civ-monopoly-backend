package me.civka.monopoly.service.mapper;

import java.util.List;
import me.civka.monopoly.dto.room.RoomCreateRequestDto;
import me.civka.monopoly.dto.room.RoomDto;
import me.civka.monopoly.dto.room.RoomListDto;
import me.civka.monopoly.repository.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = MemberMapper.class)
public interface RoomMapper {

  Room toRoomEntity(RoomCreateRequestDto roomCreateRequestDto);

  @Mapping(target = "members", source = "members", qualifiedByName = "toMemberDto")
  @Mapping(target = "hasPassword", source = "password", qualifiedByName = "checkForPassword")
  RoomDto toRoomDto(Room room);

  @Named("checkForPassword")
  default boolean checkForPassword(String password) {
    return password != null && !password.isEmpty();
  }

  List<RoomDto> toRoomDto(List<Room> rooms);

  default RoomListDto toRoomListDto(List<Room> rooms) {
    return new RoomListDto(toRoomDto(rooms));
  }
}
