package me.civka.monopoly.repository.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "rooms")
public class Room {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID roomReference;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private Integer memberLimit;

  private String password;

  @Column(nullable = false)
  private boolean isStarted;

  @OneToMany(
      mappedBy = "room",
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE},
      orphanRemoval = true)
  private List<Member> members;

  public boolean equalsById(Room room) {
    return room != null && room.getRoomReference().equals(roomReference);
  }
}
