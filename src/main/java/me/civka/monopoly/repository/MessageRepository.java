package me.civka.monopoly.repository;

import java.util.Optional;
import java.util.UUID;
import me.civka.monopoly.repository.entity.Chat;
import me.civka.monopoly.repository.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

  Optional<Message> findTopByChatOrderByTimeStampDesc(Chat chat);

  Optional<Message> findByChatReferenceAndReference(UUID chatReference, UUID reference);
}
