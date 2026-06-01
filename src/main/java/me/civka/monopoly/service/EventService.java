package me.civka.monopoly.service;

import java.util.List;
import me.civka.monopoly.dto.event.EventDto;
import me.civka.monopoly.repository.entity.Event;
import me.civka.monopoly.repository.entity.Event.EventType;
import me.civka.monopoly.repository.entity.Member;

public interface EventService {

  List<EventDto> getMyEvents();

  void handleNewPosition(Member member, int firstRoll, int secondRoll);

  void addEvent(Member member, EventType type);

  void addEvent(Member member, EventType type, int roll);

  void deleteEvent(Member member, EventType type);

  void deleteAllEvents(Member member);

  Event findByMemberAndType(Member member, EventType type);

  void skipEvent(EventType type);
}
