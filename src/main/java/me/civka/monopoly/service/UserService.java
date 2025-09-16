package me.civka.monopoly.service;

import me.civka.monopoly.dto.user.UserDto;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

  UserDto getCurrentUser();

  UserDto getUserByUsername(String username);

  UserDto updateAvatar(MultipartFile multipartFile);
}
