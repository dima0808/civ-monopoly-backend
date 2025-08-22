package me.civka.monopoly.config;

import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.repository.AuthorityRepository;
import me.civka.monopoly.repository.ChatRepository;
import me.civka.monopoly.repository.entity.Authority;
import me.civka.monopoly.repository.entity.Authority.AuthorityName;
import me.civka.monopoly.repository.entity.Chat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements ApplicationListener<ContextRefreshedEvent> {

  private final AuthorityRepository authorityRepository;

  private final ChatRepository chatRepository;

  @Value("${app.chat.public-chat-reference}")
  private String publicChatReference;

  @Override
  @Transactional
  public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {

    createRoleIfNotFound(AuthorityName.ROLE_USER);
    createRoleIfNotFound(AuthorityName.ROLE_ADMIN);
    createRoleIfNotFound(AuthorityName.ROLE_MUTED);

    createPublicChatIfNotFound();
  }

  private void createRoleIfNotFound(AuthorityName authority) {
    authorityRepository
        .findByAuthority(authority)
        .orElseGet(
            () -> authorityRepository.save(Authority.builder().authority(authority).build()));
  }

  private void createPublicChatIfNotFound() {
    if (chatRepository.existsById(UUID.fromString(publicChatReference))) {
      return;
    }

    Chat publicChat = chatRepository.save(Chat.builder().build());
    //    publicChat.setReference(UUID.fromString(publicChatReference));
    chatRepository.save(publicChat);
  }
}
