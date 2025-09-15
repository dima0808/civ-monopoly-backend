package me.civka.monopoly.repository.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.List;
import java.util.Set;
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
@Table(name = "chats")
public class Chat {

  @Id private UUID reference;

  @ManyToMany(fetch = FetchType.EAGER)
  private Set<User> users; // not null if private chat

  @OneToOne(fetch = FetchType.EAGER)
  private Room room; // not null if public chat

  @OneToMany(
      mappedBy = "chat",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  @OrderBy("timeStamp ASC")
  private List<Message> messages;

  @PrePersist
  public void generateReference() {
    if (reference == null) {
      reference = UUID.randomUUID(); // fallback
    }
  }

  public boolean isPublic() {
    return users.isEmpty();
  }

  public boolean isUserAbsent(User user) {
    return users.stream().noneMatch(u -> u.equalsById(user));
  }
}
