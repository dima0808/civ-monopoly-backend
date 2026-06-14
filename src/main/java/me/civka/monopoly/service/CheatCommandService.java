package me.civka.monopoly.service;

import me.civka.monopoly.repository.entity.Chat;
import me.civka.monopoly.repository.entity.User;

public interface CheatCommandService {

  boolean processCommand(String message, User sender, Chat chat);
}
