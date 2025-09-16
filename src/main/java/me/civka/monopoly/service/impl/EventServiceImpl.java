package me.civka.monopoly.service.impl;

import me.civka.monopoly.repository.entity.Event.EventType;
import me.civka.monopoly.service.EventService;

public class EventServiceImpl implements EventService {

  @Override
  public boolean processEvent(int choice, EventType eventType) {
    return false;
  }
}
