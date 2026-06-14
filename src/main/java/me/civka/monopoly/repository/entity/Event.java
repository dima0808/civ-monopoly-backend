package me.civka.monopoly.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "events",
    indexes = {
      @Index(name = "idx_event_member_type", columnList = "member_reference, type"),
    })
public class Event {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID reference;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EventType type;

  @Embedded private EventExtraData ext = new EventExtraData();

  @ManyToOne(fetch = FetchType.EAGER)
  private Member member;

  @Getter
  @RequiredArgsConstructor
  public enum EventType {
    BUY_PROPERTY(true),
    FOREIGN_PROPERTY(false),
    ENEMY_PROPERTY(false),
    GOODY_HUT(false),
    BARBARIANS(false),
    PROJECTS_EDGE(true),
    PROJECTS_SCIENCE(true),
    PROJECTS_CULTURE(true),
    TELEPORT(false),
    ALLIANCE(false),
    PEACE(false);

    private final boolean skippable;
  }
}
