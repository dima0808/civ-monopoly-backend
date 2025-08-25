package me.civka.monopoly.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import me.civka.monopoly.repository.entity.Chat;
import me.civka.monopoly.repository.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {

  @EntityGraph(attributePaths = {"messages.sender", "users"})
  @NonNull
  Optional<Chat> findById(@NonNull UUID reference);

  @EntityGraph(attributePaths = {"messages"})
  Optional<Chat> findChatByRoomReference(UUID roomReference);

  @EntityGraph(attributePaths = {"users"})
  List<Chat> findAllByUsersContaining(User user);

  boolean existsByUsersContainsAndReference(User user, UUID reference);

  boolean existsByUsers(List<User> users);
}
