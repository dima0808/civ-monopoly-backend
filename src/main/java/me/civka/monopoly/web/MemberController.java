package me.civka.monopoly.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.member.ChangeCivilizationRequestDto;
import me.civka.monopoly.dto.member.ChangeColorRequestDto;
import me.civka.monopoly.dto.member.MemberDto;
import me.civka.monopoly.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;

  @Operation(summary = "Change member's civilization")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Civilization changed successfully"),
        @ApiResponse(responseCode = "403", description = "Civilization already taken")
      })
  @PostMapping("/change-civilization")
  @ResponseStatus(HttpStatus.OK)
  public MemberDto changeCivilization(@RequestBody @Valid ChangeCivilizationRequestDto requestDto) {
    return memberService.changeCivilization(requestDto);
  }

  @Operation(summary = "Change member's color")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Color changed successfully"),
        @ApiResponse(responseCode = "403", description = "Color already taken")
      })
  @PostMapping("/change-color")
  @ResponseStatus(HttpStatus.OK)
  public MemberDto changeColor(@RequestBody @Valid ChangeColorRequestDto requestDto) {
    return memberService.changeColor(requestDto);
  }
}
