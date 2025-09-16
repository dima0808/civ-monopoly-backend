package me.civka.monopoly.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.NoArgsConstructor;
import me.civka.monopoly.service.AuthService;
import me.civka.monopoly.service.exception.user.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@NoArgsConstructor
public class HttpAuthTokenFilter extends OncePerRequestFilter {

  @Autowired private AuthService authService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws IOException, ServletException {

    try {
      authService.authenticate(request.getHeader(HttpHeaders.AUTHORIZATION));
    } catch (UserNotFoundException ignored) {
    }

    filterChain.doFilter(request, response);
  }
}
