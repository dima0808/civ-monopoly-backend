package me.civka.monopoly.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import me.civka.monopoly.repository.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  @EntityGraph(attributePaths = {"member.room"})
  Optional<User> findWithRoomByUsername(String username);

  Optional<User> findByUsername(String username);

  List<User> findByUsernameContainingIgnoreCase(String username);

  boolean existsByUsername(String username);
}
