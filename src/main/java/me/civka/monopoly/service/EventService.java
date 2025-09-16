package me.civka.monopoly.service;

import me.civka.monopoly.repository.entity.Event.EventType;

public interface EventService {

  boolean processEvent(int choice, EventType eventType);
}
