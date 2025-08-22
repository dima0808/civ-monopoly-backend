package me.civka.monopoly.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.civka.monopoly.repository.entity.Authority.AuthorityName;
import org.springframework.security.core.userdetails.UserDetails;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "users")
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID reference;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private String password;

  @OneToOne(fetch = FetchType.EAGER, orphanRemoval = true)
  private Member member;

  @ManyToMany(fetch = FetchType.EAGER)
  private Set<Authority> authorities;

  public boolean equalsById(User user) {
    return user != null && user.getReference().equals(reference);
  }

  public boolean hasAuthority(AuthorityName authorityName) {
    return authorities.stream().anyMatch(a -> a.getAuthority().equals(authorityName.name()));
  }
}
