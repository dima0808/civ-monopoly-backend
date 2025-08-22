package me.civka.monopoly.repository.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(name = "chats")
public class Chat {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID reference;

  @ManyToMany(fetch = FetchType.EAGER)
  private List<User> users; // not null if private chat

  @OneToOne(fetch = FetchType.EAGER)
  private Room room; // not null if public chat

  @OneToMany(
      mappedBy = "chat",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private List<Message> messages;

  public boolean isPublic() {
    return users.isEmpty();
  }

  public boolean isUserAbsent(User user) {
    return users.stream().noneMatch(u -> u.equalsById(user));
  }
}
