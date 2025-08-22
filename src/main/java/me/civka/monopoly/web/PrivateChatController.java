package me.civka.monopoly.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.chat.ChatDto;
import me.civka.monopoly.dto.chat.ChatListDto;
import me.civka.monopoly.dto.message.MessageDto;
import me.civka.monopoly.dto.message.MessageRequestDto;
import me.civka.monopoly.service.PrivateChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chats/private")
@RequiredArgsConstructor
public class PrivateChatController {

  private final PrivateChatService chatService;

  @Operation(
      summary = "Get all private chats",
      description = "Returns a list of all private chats for the authenticated user.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved list of private chats",
            content = @Content(schema = @Schema(implementation = ChatListDto.class)))
      })
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public ChatListDto getAllPrivateChats() {
    return chatService.getAllPrivateChats();
  }

  @Operation(
      summary = "Get private chat by reference",
      description = "Returns details of a private chat by its reference.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Private chat found",
            content = @Content(schema = @Schema(implementation = ChatDto.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Private chat not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
      })
  @GetMapping("/{chatReference}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public ChatDto getPrivateChatByReference(@PathVariable UUID chatReference) {
    return chatService.getPrivateChatByReference(chatReference);
  }

  @Operation(
      summary = "Create a private chat",
      description = "Creates a new private chat with the specified user.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Private chat created successfully",
            content = @Content(schema = @Schema(implementation = ChatDto.class))),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Chat already exists between the users",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
      })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('USER')")
  public ChatDto createPrivateChat(String receiverUsername) {
    return chatService.createPrivateChat(receiverUsername);
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
            description = "User not allowed to send messages in this chat",
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
      @PathVariable UUID chatReference, @RequestBody MessageRequestDto messageRequestDto) {
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
