package me.civka.monopoly.service.impl;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import me.civka.monopoly.dto.user.UserJwtTokenDto;
import me.civka.monopoly.dto.user.UserRequestDto;
import me.civka.monopoly.repository.AuthorityRepository;
import me.civka.monopoly.repository.UserRepository;
import me.civka.monopoly.repository.entity.Authority;
import me.civka.monopoly.repository.entity.Authority.AuthorityName;
import me.civka.monopoly.repository.entity.User;
import me.civka.monopoly.service.AuthService;
import me.civka.monopoly.service.exception.user.AuthorityNotFoundException;
import me.civka.monopoly.service.exception.user.UserAlreadyExistsException;
import me.civka.monopoly.util.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final AuthorityRepository authorityRepository;
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;
  private final PasswordEncoder passwordEncoder;
  private final UserDetailsService userDetailsService;

  @Override
  public UserJwtTokenDto register(UserRequestDto userRequestDto) {
    if (userRepository.existsByUsername(userRequestDto.getUsername())) {
      throw new UserAlreadyExistsException(userRequestDto.getUsername());
    }

    Authority authority =
        authorityRepository
            .findByAuthority(AuthorityName.ROLE_USER)
            .orElseThrow(() -> new AuthorityNotFoundException(AuthorityName.ROLE_USER));
    User user =
        User.builder()
            .username(userRequestDto.getUsername())
            .password(passwordEncoder.encode(userRequestDto.getPassword()))
            .authorities(Set.of(authority))
            .build();

    userRepository.save(user);

    return login(userRequestDto);
  }

  @Override
  public UserJwtTokenDto login(UserRequestDto userRequestDto) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                userRequestDto.getUsername(), userRequestDto.getPassword()));
    return new UserJwtTokenDto(jwtUtils.generateToken(authentication));
  }

  public Authentication authenticate(String authorizationHeader) {
    String token = jwtUtils.parseJwt(authorizationHeader);

    if (token != null && jwtUtils.validateToken(token)) {
      String username = jwtUtils.extractClaims(token).getSubject();
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);
      if (userDetails == null) {
        return null;
      }
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    return SecurityContextHolder.getContext().getAuthentication();
  }
}
