package me.civka.monopoly.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "members")
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

  public enum Civilization {
    RANDOM,
    COLOMBIA,
    EGYPT,
    GERMANY,
    JAPAN,
    KOREA,
    ROME,
    SWEDEN,
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
