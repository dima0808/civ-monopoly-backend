package me.civka.monopoly.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "messages",
    indexes = {
      @Index(name = "idx_message_chat_timestamp", columnList = "chat_reference, time_stamp DESC"),
      @Index(name = "idx_message_chat_ref", columnList = "chat_reference, reference"),
    })
public class Message {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID reference;

  @Column(nullable = false)
  private String message;

  @Column(nullable = false)
  private OffsetDateTime timeStamp;

  @ManyToOne(fetch = FetchType.LAZY)
  private Chat chat;

  @ManyToOne(fetch = FetchType.EAGER)
  private User sender;
}
