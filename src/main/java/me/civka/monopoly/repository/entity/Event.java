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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "events")
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

  public enum EventType {
    BUY_PROPERTY,
    FOREIGN_PROPERTY,
    ENEMY_PROPERTY,
    GOODY_HUT,
    BARBARIANS,
    PROJECTS_EDGE,
    PROJECTS_SCIENCE,
    PROJECTS_CULTURE,
    ALLIANCE,
    PEACE,
  }
}
