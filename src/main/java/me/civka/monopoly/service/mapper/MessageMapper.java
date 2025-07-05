package me.civka.monopoly.service.mapper;

import java.util.List;
import me.civka.monopoly.dto.message.MessageDto;
import me.civka.monopoly.dto.message.MessageRequestDto;
import me.civka.monopoly.repository.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface MessageMapper {

  Message toMessageEntity(MessageRequestDto messageDto);

  @Mapping(target = "sender", source = "sender.username")
  MessageDto toMessageDto(Message message);

  @Named("toMessageDto")
  List<MessageDto> toMessageDto(List<Message> message);
}
