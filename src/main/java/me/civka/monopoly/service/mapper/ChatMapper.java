package me.civka.monopoly.service.mapper;

import java.util.List;
import me.civka.monopoly.dto.chat.ChatDto;
import me.civka.monopoly.repository.entity.Chat;
import me.civka.monopoly.repository.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(
    componentModel = "spring",
    uses = {MessageMapper.class})
public interface ChatMapper {

  @Mapping(target = "users", source = "users", qualifiedByName = "toUsername")
  @Mapping(target = "messages", source = "messages", qualifiedByName = "toMessageDto")
  ChatDto toChatDto(Chat chat);

  @Named("toUsername")
  default List<String> toUsername(List<User> users) {
    return users.stream().map(User::getUsername).toList();
  }
}
