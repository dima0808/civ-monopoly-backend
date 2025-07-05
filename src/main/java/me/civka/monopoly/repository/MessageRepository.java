package me.civka.monopoly.repository;

import java.util.UUID;
import me.civka.monopoly.repository.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {}
