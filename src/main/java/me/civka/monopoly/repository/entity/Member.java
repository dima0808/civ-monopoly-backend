package me.civka.monopoly.repository.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.civka.monopoly.common.ScienceProject;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(
    name = "members",
    indexes = {
      @Index(name = "idx_member_room", columnList = "room_reference"),
      @Index(name = "idx_member_user", columnList = "user_reference"),
    })
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID reference;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Civilization civilization;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Color color;

  @Column(nullable = false)
  private Integer position;

  @Column(nullable = false)
  private Integer roundsMade;

  @Column(nullable = false)
  private Integer gold;

  @Column(nullable = false)
  private Integer strength;

  @Column(nullable = false)
  private Integer tourism;

  @Column(nullable = false)
  private Integer score;

  @Column(nullable = false)
  private OffsetDateTime joinedAt;

  @OneToOne(fetch = FetchType.EAGER)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  private Room room;

  @OneToMany(
      mappedBy = "member",
      fetch = FetchType.EAGER,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private List<Event> events = new ArrayList<>();

  @ElementCollection(targetClass = ScienceProject.class, fetch = FetchType.EAGER)
  @Enumerated(EnumType.STRING)
  private List<ScienceProject> finishedScienceProjects = new ArrayList<>();

  @Column(nullable = false)
  private Integer turnsToNextScienceProject;

  @Column(nullable = false)
  private Integer expeditionTurns;

  @OneToMany(
      mappedBy = "member",
      fetch = FetchType.EAGER,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private List<AdditionalEffect> additionalEffects = new ArrayList<>();

  public enum Civilization {
    RANDOM,
    AMERICA,
    AZTEC,
    BABYLON,
    COLOMBIA,
    EGYPT,
    ENGLAND,
    FRANCE,
    GAUL,
    JAPAN,
    OTTOMAN,
  }

  public enum Color {
    RED,
    BLUE,
    GREEN,
    YELLOW,
    TURQUOISE,
    ORANGE,
    PINK,
    VIOLET,
  }

  public boolean equalsById(Member member) {
    return member != null && member.getReference().equals(reference);
  }
}
