package me.civka.monopoly.service.mapper;

import java.util.List;
import me.civka.monopoly.dto.room.RoomCreateRequestDto;
import me.civka.monopoly.dto.room.RoomDto;
import me.civka.monopoly.dto.room.RoomListDto;
import me.civka.monopoly.repository.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = MemberMapper.class)
public interface RoomMapper {

  @Mapping(target = "members", expression = "java(new java.util.ArrayList<>())")
  @Mapping(target = "isStarted", constant = "false")
  @Mapping(target = "turn", constant = "-1")
  Room toRoomEntity(RoomCreateRequestDto roomCreateRequestDto);

  @Mapping(target = "members", source = "members", qualifiedByName = "toMemberDto")
  @Mapping(target = "ext", ignore = true)
  RoomDto toRoomDto(Room room);

  List<RoomDto> toRoomDto(List<Room> rooms);

  default RoomListDto toRoomListDto(List<Room> rooms) {
    return new RoomListDto(toRoomDto(rooms));
  }
}
