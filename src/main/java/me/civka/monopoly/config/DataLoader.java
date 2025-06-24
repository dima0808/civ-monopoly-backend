package me.civka.monopoly.config;

import lombok.RequiredArgsConstructor;
import me.civka.monopoly.repository.AuthorityRepository;
import me.civka.monopoly.repository.entity.Authority;
import me.civka.monopoly.repository.entity.Authority.AuthorityName;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements ApplicationListener<ContextRefreshedEvent> {

  private final AuthorityRepository authorityRepository;

  @Override
  public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {

    createRoleIfNotFound(AuthorityName.ROLE_USER);
    createRoleIfNotFound(AuthorityName.ROLE_ADMIN);
  }

  private void createRoleIfNotFound(AuthorityName authority) {
    authorityRepository
        .findByAuthority(authority)
        .orElseGet(
            () -> {
              Authority newAuthority = Authority.builder().authority(authority).build();
              return authorityRepository.save(newAuthority);
            });
  }
}
