package me.civka.monopoly.service.impl;

import static me.civka.monopoly.util.GameUtils.getMemberFromAuthentication;

import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.member.ChangeCivilizationRequestDto;
import me.civka.monopoly.dto.member.ChangeColorRequestDto;
import me.civka.monopoly.dto.member.MemberDto;
import me.civka.monopoly.message.RoomMessage;
import me.civka.monopoly.message.RoomMessage.MessageType;
import me.civka.monopoly.repository.MemberRepository;
import me.civka.monopoly.repository.RoomRepository;
import me.civka.monopoly.repository.entity.Member;
import me.civka.monopoly.repository.entity.Member.Civilization;
import me.civka.monopoly.repository.entity.Member.Color;
import me.civka.monopoly.repository.entity.Room;
import me.civka.monopoly.service.MemberService;
import me.civka.monopoly.service.exception.room.RoomNotFoundException;
import me.civka.monopoly.service.exception.user.UserNotAllowedException;
import me.civka.monopoly.service.mapper.MemberMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

  private final MemberMapper memberMapper;
  private final MemberRepository memberRepository;
  private final RoomRepository roomRepository;
  private final SimpMessagingTemplate messagingTemplate;

  @Override
  public MemberDto changeCivilization(ChangeCivilizationRequestDto requestDto) {
    Civilization civilization = Civilization.valueOf(requestDto.civilization());
    Member member = getMemberFromAuthentication();
    Room room =
        roomRepository
            .getRoomByMembersContaining(member)
            .orElseThrow(() -> new RoomNotFoundException(member));

    if (room.getMembers().stream()
        .anyMatch(
            m -> m.getCivilization() == civilization && civilization != Civilization.RANDOM)) {
      throw new UserNotAllowedException("Civilization already taken in this room.");
    }

    member.setCivilization(civilization);
    MemberDto memberDto = memberMapper.toMemberDto(memberRepository.save(member));

    convertAndSendTo(
        "/topic/rooms/" + room.getReference(), memberDto, MessageType.CHANGE_CIVILIZATION);

    return memberDto;
  }

  @Override
  public MemberDto changeColor(ChangeColorRequestDto requestDto) {
    Color color = Color.valueOf(requestDto.color());
    Member member = getMemberFromAuthentication();
    Room room =
        roomRepository
            .getRoomByMembersContaining(member)
            .orElseThrow(() -> new RoomNotFoundException(member));

    if (room.getMembers().stream().anyMatch(m -> m.getColor() == color)) {
      throw new UserNotAllowedException("Color already taken in this room.");
    }

    member.setColor(color);
    MemberDto memberDto = memberMapper.toMemberDto(memberRepository.save(member));

    convertAndSendTo("/topic/rooms/" + room.getReference(), memberDto, MessageType.CHANGE_COLOR);

    return memberDto;
  }

  private void convertAndSendTo(String destination, MemberDto memberDto, MessageType type) {
    messagingTemplate.convertAndSend(destination, RoomMessage.of(memberDto, type));
  }
}
