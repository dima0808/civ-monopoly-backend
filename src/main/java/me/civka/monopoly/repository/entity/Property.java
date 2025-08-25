package me.civka.monopoly.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.List;
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
@Table(name = "properties")
public class Property {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID reference;

  @Column(nullable = false)
  private Integer position;

  @Column(nullable = false)
  private Integer mortgage;

  @Column(nullable = false)
  private Integer roundOfLastChange;

  @Column(nullable = false)
  private Integer turnOfLastChange;

  @ElementCollection(targetClass = UpgradeType.class, fetch = FetchType.EAGER)
  @Enumerated(EnumType.STRING)
  private List<UpgradeType> upgrades;

  @ManyToOne(fetch = FetchType.EAGER)
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  private Room room;

  public enum UpgradeType {
    LEVEL_1,
    LEVEL_2,
    LEVEL_3,
  }

  public enum BonusType {
    TEMPLE_OF_ARTEMIS,
    CASA_DE_CONTRATACION,
    COLOSSEUM,
    ETEMENANKI,
    MAUSOLEUM_AT_HALICARNASSUS,
    RUHR_VALLEY,
    ESTADIO_DO_MARACANA,
    GOVERNMENT_PLAZA,
    IRON,
    FABRIC,
    SHIPYARD,
    REEF,
    WONDER,
    ENTERTAINMENT_COMPLEX,
    FARMS,
    AQUEDUCT,
    DAM,
  }
}
