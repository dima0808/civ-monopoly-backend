package me.civka.monopoly.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.repository.entity.Event.EventType;
import me.civka.monopoly.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

  private final EventService eventService;

  @Operation(summary = "Skip an event")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Event skipped successfully"),
        @ApiResponse(
            responseCode = "403",
            description = "Event cannot be skipped or does not exist")
      })
  @PostMapping("/skip/{type}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public void skipEvent(
      @Parameter(description = "Event type to skip", required = true) @PathVariable
          EventType type) {
    eventService.skipEvent(type);
  }
}
