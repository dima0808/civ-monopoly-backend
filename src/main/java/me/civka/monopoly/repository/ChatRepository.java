package me.civka.monopoly.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import me.civka.monopoly.repository.entity.Chat;
import me.civka.monopoly.repository.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, UUID> {

  @EntityGraph(attributePaths = {"messages", "messages.sender"})
  Optional<Chat> findChatByRoomReference(UUID roomReference);

  boolean existsByUsers(List<User> users);
}
