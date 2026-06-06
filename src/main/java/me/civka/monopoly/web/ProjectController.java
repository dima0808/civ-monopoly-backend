package me.civka.monopoly.web;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.member.AdditionalEffectDto;
import me.civka.monopoly.dto.member.MemberDto;
import me.civka.monopoly.dto.project.ProjectChoiceRequestDto;
import me.civka.monopoly.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

  private final ProjectService projectService;

  @Operation(summary = "Choose a district-enhancement project")
  @PostMapping("/choose")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public MemberDto chooseProject(@RequestBody @Valid ProjectChoiceRequestDto request) {
    return projectService.chooseProject(request);
  }

  @Operation(summary = "Perform the next science project")
  @PostMapping("/science")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public MemberDto doScienceProject() {
    return projectService.doScienceProject();
  }

  @Operation(summary = "Perform a concert (culture project)")
  @PostMapping("/concert")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public MemberDto doConcert() {
    return projectService.doConcert();
  }

  @Operation(summary = "Get my active additional effects")
  @GetMapping("/effects")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public List<AdditionalEffectDto> getMyAdditionalEffects() {
    return projectService.getMyAdditionalEffects();
  }
}
