package me.civka.monopoly.repository.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "rooms")
public class Room {

  @Id private UUID reference;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private Integer memberLimit;

  private String password;

  @Embedded private RoomState state;

  @Column(nullable = false)
  private Boolean isStarted = false;

  @Column(nullable = false)
  private Integer turnIndex = -1;

  @Column(nullable = false)
  private Boolean isDiceRolled = false;

  @Column(nullable = false)
  private Integer turn = 0;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private Chat chat;

  @OneToMany(
      mappedBy = "room",
      fetch = FetchType.EAGER,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE},
      orphanRemoval = true)
  @OrderBy("joinedAt ASC")
  private List<Member> members = new ArrayList<>();

  @PrePersist
  public void prePersist() {
    if (reference == null) {
      reference = UUID.randomUUID(); // fallback
    }
  }

  public boolean equalsById(Room room) {
    return room != null && room.getReference().equals(reference);
  }

  public boolean isOwnedBy(User user) {
    return members.getFirst().getUser().equalsById(user);
  }
}
