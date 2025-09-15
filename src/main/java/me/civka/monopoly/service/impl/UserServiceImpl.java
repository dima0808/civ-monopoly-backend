package me.civka.monopoly.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.user.UserDto;
import me.civka.monopoly.repository.UserRepository;
import me.civka.monopoly.repository.entity.User;
import me.civka.monopoly.service.UserService;
import me.civka.monopoly.service.mapper.UserMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private static final String RESOURCES_PATH = "src/main/webapp/WEB-INF";
  public static final String AVATAR_URL_PREFIX = "/images/avatars";
  private static final String AVATAR_BASE_URL = "src/main/webapp/WEB-INF/images/avatars";

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  public UserDto getCurrentUser() {
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return userMapper.toUserDto(user);
  }

  @Override
  public UserDto updateAvatar(MultipartFile multipartFile) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    if (multipartFile != null) {
      deleteLastAvatar(user.getAvatarUrl());
      user.setAvatarUrl(saveAvatar(multipartFile));
    }

    return userMapper.toUserDto(userRepository.save(user));
  }

  private void deleteLastAvatar(String fileUrl) {
    if (fileUrl == null || fileUrl.isBlank()) {
      return;
    }

    try {
      String filename = Paths.get(RESOURCES_PATH + fileUrl).getFileName().toString();

      Path filePath = Paths.get(AVATAR_BASE_URL, filename);
      Files.deleteIfExists(filePath);

    } catch (IOException e) {
      throw new RuntimeException("Error with file deleting:" + fileUrl, e);
    }
  }

  private String saveAvatar(MultipartFile multipartFile) {
    File file = new File(AVATAR_BASE_URL);

    if (!file.exists() && !file.mkdirs()) {
      throw new RuntimeException("Could not create directory for avatars");
    }

    String filename = UUID.randomUUID() + "_" + multipartFile.getOriginalFilename();
    Path filePath = Path.of(AVATAR_BASE_URL, filename);

    try {
      Files.write(filePath, multipartFile.getBytes());
      return AVATAR_URL_PREFIX + "/" + filename;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
