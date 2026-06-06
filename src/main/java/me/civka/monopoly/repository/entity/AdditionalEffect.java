package me.civka.monopoly.repository.entity;

import jakarta.persistence.Column;
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
import lombok.Setter;
import me.civka.monopoly.common.AdditionalEffectType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(
    name = "additional_effects",
    indexes = {
      @Index(name = "idx_additional_effect_member", columnList = "member_reference"),
    })
public class AdditionalEffect {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID reference;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AdditionalEffectType type;

  /** -1 = permanent, > 0 = remaining turns, 0 = expired (removed on turn tick). */
  @Column(nullable = false)
  private Integer turnsLeft;

  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;
}
