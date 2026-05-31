package me.civka.monopoly.message;

import me.civka.monopoly.dto.event.EventDto;

public record EventMessage(EventDto event, MessageType type) {

  public static EventMessage of(EventDto event, MessageType type) {
    return new EventMessage(event, type);
  }

  public static EventMessage of(MessageType type) {
    return new EventMessage(null, type);
  }

  public enum MessageType {
    ADD_EVENT,
    DELETE_EVENT,
    DELETE_ALL_EVENTS,
  }
}
