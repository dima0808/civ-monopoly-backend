package me.civka.monopoly.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Entity
@Table(name = "authorities")
public class Authority implements GrantedAuthority {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID authorityReference;

  @Column(nullable = false, unique = true)
  @Enumerated(EnumType.STRING)
  private AuthorityName authority;

  @Override
  public String getAuthority() {
    return authority.name();
  }

  public enum AuthorityName {
    ROLE_USER,
    ROLE_ADMIN,
  }
}
