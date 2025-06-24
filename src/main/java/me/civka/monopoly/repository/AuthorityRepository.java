package me.civka.monopoly.repository;

import java.util.Optional;
import java.util.UUID;
import me.civka.monopoly.repository.entity.Authority;
import me.civka.monopoly.repository.entity.Authority.AuthorityName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, UUID> {

  Optional<Authority> findByAuthority(AuthorityName authority);
}
