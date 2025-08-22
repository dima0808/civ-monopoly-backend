package me.civka.monopoly.service;

public interface PublicChatService extends ChatService {

  void muteUser(String username);

  void unmuteUser(String username);
}
