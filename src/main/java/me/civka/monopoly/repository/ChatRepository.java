package me.civka.monopoly.repository;

import java.util.UUID;
import me.civka.monopoly.repository.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, UUID> {
}
