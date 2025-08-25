package me.civka.monopoly.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.room.RoomCreateRequestDto;
import me.civka.monopoly.dto.room.RoomDto;
import me.civka.monopoly.dto.room.RoomJoinRequestDto;
import me.civka.monopoly.dto.room.RoomListDto;
import me.civka.monopoly.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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

  @Operation(summary = "Get all rooms", description = "Returns a list of all available rooms.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved list of rooms",
            content = @Content(schema = @Schema(implementation = RoomListDto.class)))
      })
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public RoomListDto getAllRooms() {
    return roomService.getAllRooms();
  }

  @Operation(
      summary = "Get room by reference",
      description = "Returns details of a room by its reference.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Room found",
            content = @Content(schema = @Schema(implementation = RoomDto.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Room not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
      })
  @GetMapping("/{roomReference}")
  @ResponseStatus(HttpStatus.OK)
  public RoomDto getRoomByReference(
      @Parameter(description = "Room reference UUID", required = true) @PathVariable
          UUID roomReference) {
    return roomService.getRoomByReference(roomReference);
  }

  @Operation(
      summary = "Create a new room",
      description = "Creates a new room with the specified parameters.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Room created",
            content = @Content(schema = @Schema(implementation = RoomDto.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
      })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('USER')")
  public RoomDto createRoom(
      @Parameter(description = "Room create request", required = true) @RequestBody @Valid
          RoomCreateRequestDto roomCreateRequestDto) {
    return roomService.createRoom(roomCreateRequestDto);
  }

  @Operation(summary = "Join a room", description = "Join a room by reference and password.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully joined the room",
            content = @Content(schema = @Schema(implementation = RoomDto.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid password or room is full",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Room not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
      })
  @PostMapping("/join/{roomReference}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public RoomDto joinRoom(
      @Parameter(description = "Room reference UUID", required = true) @PathVariable
          UUID roomReference,
      @Parameter(description = "Room join request", required = true) @RequestBody
          RoomJoinRequestDto roomJoinRequestDto) {
    return roomService.joinRoom(roomReference, roomJoinRequestDto.getPassword());
  }

  @Operation(
      summary = "Leave current room",
      description = "Leaves the room the user is currently in.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully left the room",
            content = @Content(schema = @Schema(implementation = RoomDto.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Room not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
      })
  @PostMapping("/leave")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public RoomDto leaveRoom() {
    return roomService.leaveRoom();
  }

  @Operation(
      summary = "Kick user from room",
      description = "Kicks a user from the current room (owner only).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User kicked from room",
            content = @Content(schema = @Schema(implementation = RoomDto.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Not allowed",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
            responseCode = "404",
            description = "User or room not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
      })
  @PostMapping("/kick/{memberReference}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public RoomDto kickMember(
      @Parameter(description = "Member reference UUID", required = true) @PathVariable
          UUID memberReference) {
    return roomService.kickMember(memberReference);
  }
}
