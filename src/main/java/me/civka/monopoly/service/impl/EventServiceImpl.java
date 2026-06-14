package me.civka.monopoly.service.impl;

import static me.civka.monopoly.util.GameUtils.getMemberFromAuthentication;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.common.PropertyType;
import me.civka.monopoly.config.ConfigurationHolder;
import me.civka.monopoly.config.properties.PropertiesConfiguration;
import me.civka.monopoly.config.properties.PropertyDetails;
import me.civka.monopoly.dto.event.EventDto;
import me.civka.monopoly.message.EventMessage;
import me.civka.monopoly.message.EventMessage.MessageType;
import me.civka.monopoly.repository.EventRepository;
import me.civka.monopoly.repository.MemberRepository;
import me.civka.monopoly.repository.PropertyRepository;
import me.civka.monopoly.repository.entity.Event;
import me.civka.monopoly.repository.entity.Event.EventType;
import me.civka.monopoly.repository.entity.EventExtraData;
import me.civka.monopoly.repository.entity.Member;
import me.civka.monopoly.repository.entity.Property;
import me.civka.monopoly.repository.entity.Room;
import me.civka.monopoly.service.EventService;
import me.civka.monopoly.service.exception.user.UserNotAllowedException;
import me.civka.monopoly.service.mapper.EventMapper;
import me.civka.monopoly.util.GameUtils;
import me.civka.monopoly.util.PropertyUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

  private final EventRepository eventRepository;
  private final PropertyRepository propertyRepository;
  private final MemberRepository memberRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final EventMapper eventMapper;

  private final PropertiesConfiguration propertiesConfiguration =
      ConfigurationHolder.propertiesConfiguration();

  private static final int TELEPORT_POSITION = 24;

  private static final List<Integer> PROJECTS_EDGE_POSITIONS = List.of(13, 37);

  private static final List<Integer> PROJECT_DISTRICT_POSITIONS =
      List.of(7, 10, 15, 17, 19, 21, 22, 30, 31, 34, 38, 39, 43, 45, 47);

  @Override
  public List<EventDto> getMyEvents() {
    Member member = getMemberFromAuthentication();
    return member.getEvents().stream().map(eventMapper::toEventDto).toList();
  }

  @Override
  public void handleNewPosition(Member member, int firstRoll, int secondRoll) {
    int position = member.getPosition();
    Room room = member.getRoom();

    if (position == TELEPORT_POSITION) {
      List<Integer> candidates =
          new ArrayList<>(propertiesConfiguration.properties().keySet());
      candidates.remove(Integer.valueOf(TELEPORT_POSITION));
      Collections.shuffle(candidates);
      int pos1 = candidates.get(0);
      int pos2 = candidates.get(1);
      int pos3 = candidates.get(2);
      addTeleportEvent(member, pos1, pos2, pos3);
      return;
    }

    if (PROJECTS_EDGE_POSITIONS.contains(position)) {
      if (hasDistrictForProjects(member)) {
        addEvent(member, EventType.PROJECTS_EDGE);
      }
      return;
    }

    PropertyDetails propertyDetails = propertiesConfiguration.properties().get(position);
    if (propertyDetails == null) {
      return;
    }

    Optional<Property> propertyAtPosition =
        propertyRepository.getPropertyByPositionAndRoom(position, room);

    if (propertyAtPosition.isEmpty()) {
      addEvent(member, EventType.BUY_PROPERTY);
      return;
    }

    Property property = propertyAtPosition.get();
    if (!property.getMember().equalsById(member) && property.getMortgage() == -1) {
      if (propertyDetails.type() == PropertyType.DISTRICT_ENCAMPMENT) {
        addEvent(member, EventType.FOREIGN_PROPERTY, firstRoll + secondRoll);
      } else {
        addEvent(member, EventType.FOREIGN_PROPERTY);
      }

      Member owner = property.getMember();
      int tourismGain = PropertyUtils.calculateTourismOnStep(property);
      owner.setTourism(owner.getTourism() + tourismGain);
      memberRepository.save(owner);
    }
  }

  @Override
  public void addEvent(Member member, EventType type) {
    addEvent(member, type, 0);
  }

  @Override
  public void addEvent(Member member, EventType type, int roll) {
    Event event = Event.builder().type(type).ext(new EventExtraData(roll)).member(member).build();

    event = eventRepository.save(event);
    member.getEvents().add(event);

    EventDto eventDto = eventMapper.toEventDto(event);
    sendEventToUser(member, EventMessage.of(eventDto, MessageType.ADD_EVENT));
  }

  @Override
  public void deleteEvent(Member member, EventType type) {
    Event event =
        member.getEvents().stream().filter(e -> e.getType() == type).findFirst().orElse(null);

    if (event != null) {
      EventDto eventDto = eventMapper.toEventDto(event);
      member.getEvents().remove(event);
      memberRepository.save(member);

      sendEventToUser(member, EventMessage.of(eventDto, MessageType.DELETE_EVENT));
    }
  }

  @Override
  public void deleteAllEvents(Member member) {
    member.getEvents().clear();
    memberRepository.save(member);

    sendEventToUser(member, EventMessage.of(MessageType.DELETE_ALL_EVENTS));
  }

  @Override
  public Event findByMemberAndType(Member member, EventType type) {
    return eventRepository.findByMemberAndType(member, type).orElse(null);
  }

  @Override
  public void skipEvent(EventType type) {
    if (!type.isSkippable()) {
      throw new UserNotAllowedException("Event " + type + " cannot be skipped.");
    }

    Member member = GameUtils.getMemberFromAuthentication();
    Event event = findByMemberAndType(member, type);
    if (event == null) {
      throw new UserNotAllowedException("No " + type + " event to skip.");
    }

    deleteEvent(member, type);
  }

  private void addTeleportEvent(Member member, int pos1, int pos2, int pos3) {
    EventExtraData ext = new EventExtraData(pos1, pos2, pos3);
    Event event = Event.builder().type(EventType.TELEPORT).ext(ext).member(member).build();

    event = eventRepository.save(event);
    member.getEvents().add(event);

    EventDto eventDto = eventMapper.toEventDto(event);
    sendEventToUser(member, EventMessage.of(eventDto, MessageType.ADD_EVENT));
  }

  private boolean hasDistrictForProjects(Member member) {
    return propertyRepository.getPropertiesByMember(member).stream()
        .anyMatch(property -> PROJECT_DISTRICT_POSITIONS.contains(property.getPosition()));
  }

  private void sendEventToUser(Member member, EventMessage eventMessage) {
    String username = member.getUser().getUsername();
    messagingTemplate.convertAndSendToUser(username, "/events", eventMessage);
  }
}
