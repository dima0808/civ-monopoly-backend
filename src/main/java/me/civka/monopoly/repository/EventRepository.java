package me.civka.monopoly.repository;

import java.util.Optional;
import java.util.UUID;
import me.civka.monopoly.repository.entity.Event;
import me.civka.monopoly.repository.entity.Event.EventType;
import me.civka.monopoly.repository.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

  Optional<Event> findByMemberAndType(Member member, EventType type);

  boolean existsByMemberAndType(Member member, EventType type);
}
