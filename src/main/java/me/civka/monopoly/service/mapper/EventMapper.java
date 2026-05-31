package me.civka.monopoly.service.mapper;

import me.civka.monopoly.dto.event.EventDto;
import me.civka.monopoly.repository.entity.Event;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {

  EventDto toEventDto(Event event);
}
