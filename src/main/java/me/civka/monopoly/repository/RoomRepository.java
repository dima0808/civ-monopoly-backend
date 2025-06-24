package me.civka.monopoly.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import me.civka.monopoly.repository.entity.Room;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

  @EntityGraph(attributePaths = {"members", "members.user"})
  @NonNull
  Optional<Room> findById(@NonNull UUID roomReference);

  @EntityGraph(attributePaths = {"members", "members.user"})
  @NonNull
  List<Room> findAll();
}
