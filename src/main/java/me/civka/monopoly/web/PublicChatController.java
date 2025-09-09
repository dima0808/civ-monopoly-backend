package me.civka.monopoly.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.chat.ChatDto;
import me.civka.monopoly.dto.message.MessageDto;
import me.civka.monopoly.dto.message.MessageRequestDto;
import me.civka.monopoly.service.PublicChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chats/public")
@RequiredArgsConstructor
public class PublicChatController {

  private final PublicChatService chatService;

  @Operation(
      summary = "Get public chat by reference",
      description = "Retrieves the public chat details chat by its reference.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Public chat retrieved successfully",
            content = @Content(schema = @Schema(implementation = ChatDto.class))),
        @ApiResponse(
            responseCode = "403",
            description = "It is not a public chat",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Chat not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
      })
  @GetMapping("/{chatReference}")
  @ResponseStatus(HttpStatus.OK)
  public ChatDto getChatByReference(@PathVariable UUID chatReference) {
    return chatService.getPublicChatByReference(chatReference);
  }

  @Operation(
      summary = "Mute a user in the public chat",
      description = "Mutes a user in the public chat, preventing them from sending messages.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "User muted successfully"),
        @ApiResponse(
            responseCode = "403",
            description = "User not allowed to mute other users",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
      })
  @PutMapping("/mute/{username}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ADMIN')")
  public void muteUser(@PathVariable String username) {
    chatService.muteUser(username);
  }

  @Operation(
      summary = "Unmute a user in the public chat",
      description = "Unmutes a user in the public chat, allowing them to send messages again.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "User unmuted successfully"),
        @ApiResponse(
            responseCode = "403",
            description = "User not allowed to unmute other users",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
      })
  @PutMapping("/unmute/{username}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ADMIN')")
  public void unmuteUser(@PathVariable String username) {
    chatService.unmuteUser(username);
  }

  @Operation(
      summary = "Send a message in a private chat",
      description = "Sends a message in the specified private chat.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Message sent successfully",
            content = @Content(schema = @Schema(implementation = MessageDto.class))),
        @ApiResponse(
            responseCode = "403",
            description = "User is muted",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Chat not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
      })
  @PostMapping("/{chatReference}")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('USER')")
  public MessageDto sendMessage(
      @PathVariable UUID chatReference, @RequestBody @Valid MessageRequestDto messageRequestDto) {
    return chatService.sendMessage(chatReference, messageRequestDto);
  }

  @Operation(
      summary = "Delete a message in a private chat",
      description = "Deletes a message from the specified private chat.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Message deleted successfully"),
        @ApiResponse(
            responseCode = "403",
            description = "User not allowed to delete this message",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
      })
  @DeleteMapping("/{chatReference}/{messageReference}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('USER')")
  public void deleteMessage(@PathVariable UUID chatReference, @PathVariable UUID messageReference) {
    chatService.deleteMessage(chatReference, messageReference);
  }
}
