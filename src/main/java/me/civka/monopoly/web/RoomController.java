package me.civka.monopoly.web;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.room.RoomCreateRequestDto;
import me.civka.monopoly.dto.room.RoomDto;
import me.civka.monopoly.dto.room.RoomJoinRequestDto;
import me.civka.monopoly.dto.room.RoomListDto;
import me.civka.monopoly.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

  private final RoomService roomService;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public RoomListDto getAllRooms() {
    return roomService.getAllRooms();
  }

  @GetMapping("/{roomReference}")
  @ResponseStatus(HttpStatus.OK)
  public RoomDto getRoomByReference(@PathVariable UUID roomReference) {
    return roomService.getRoomByReference(roomReference);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('USER')")
  public RoomDto createRoom(@RequestBody @Valid RoomCreateRequestDto roomCreateRequestDto) {
    return roomService.createRoom(roomCreateRequestDto);
  }

  @PostMapping("/join/{roomReference}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public RoomDto joinRoom(
      @PathVariable UUID roomReference, @RequestBody @Valid RoomJoinRequestDto roomJoinRequestDto) {
    return roomService.joinRoom(roomReference, roomJoinRequestDto.getPassword());
  }

  @PostMapping("/leave")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public RoomDto leaveRoom() {
    return roomService.leaveRoom();
  }

  @PostMapping("/kick/{userReference}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public RoomDto kickUser(@PathVariable UUID userReference) {
    return roomService.kickUser(userReference);
  }
}
