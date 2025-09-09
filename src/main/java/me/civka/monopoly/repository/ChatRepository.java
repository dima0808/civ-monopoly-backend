package me.civka.monopoly.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import me.civka.monopoly.repository.entity.Chat;
import me.civka.monopoly.repository.entity.Room;
import me.civka.monopoly.repository.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

  @Query(
      value =
          """
      SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
      FROM Chat c
      JOIN c.users u1
      JOIN c.users u2
      WHERE u1.reference = :ref1 AND u2.reference = :ref2
      """)
  boolean existsByUsersReference(@Param("ref1") UUID userRef1, @Param("ref2") UUID userRef2);

  boolean existsByUsersAndRoom(List<User> users, Room room);
}
