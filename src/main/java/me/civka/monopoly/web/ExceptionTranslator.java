package me.civka.monopoly.web;

import static java.net.URI.create;
import static me.civka.monopoly.util.ErrorUtils.getErrorResponseEntity;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.ProblemDetail.forStatusAndDetail;

import java.util.stream.Collectors;
import me.civka.monopoly.service.exception.AuthorityNotFoundException;
import me.civka.monopoly.service.exception.IllegalMemberLimitException;
import me.civka.monopoly.service.exception.InvalidRoomPasswordException;
import me.civka.monopoly.service.exception.MemberNotFoundException;
import me.civka.monopoly.service.exception.MemberNotInRoomException;
import me.civka.monopoly.service.exception.RoomIsFullException;
import me.civka.monopoly.service.exception.RoomNotFoundException;
import me.civka.monopoly.service.exception.UserAlreadyExistsException;
import me.civka.monopoly.service.exception.UserAlreadyInRoomException;
import me.civka.monopoly.service.exception.UserNotAllowedException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ExceptionTranslator extends ResponseEntityExceptionHandler {

  private static final String VALIDATION_MESSAGE_DELIMITER = ", ";

  @ExceptionHandler(AuthorityNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleAuthorityNotFoundException(
      AuthorityNotFoundException ex) {
    return getErrorResponseEntity(NOT_FOUND, "Authority Not Found", ex);
  }

  @ExceptionHandler(RoomNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleRoomNotFoundException(RoomNotFoundException ex) {
    return getErrorResponseEntity(NOT_FOUND, "Room Not Found", ex);
  }

  @ExceptionHandler(MemberNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleMemberNotFoundException(MemberNotFoundException ex) {
    return getErrorResponseEntity(NOT_FOUND, "Member Not Found", ex);
  }

  @ExceptionHandler(MemberNotInRoomException.class)
  public ResponseEntity<ProblemDetail> handleMemberNotInRoomException(MemberNotInRoomException ex) {
    return getErrorResponseEntity(NOT_FOUND, "Member Not In Room", ex);
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleUsernameNotFoundException(
      UsernameNotFoundException ex) {
    return getErrorResponseEntity(NOT_FOUND, "Username Not Found", ex);
  }

  @ExceptionHandler(IllegalMemberLimitException.class)
  public ResponseEntity<ProblemDetail> handleIllegalMemberLimitException(
      IllegalMemberLimitException ex) {
    return getErrorResponseEntity(BAD_REQUEST, "Illegal Member Limit", ex);
  }

  @ExceptionHandler(RoomIsFullException.class)
  public ResponseEntity<ProblemDetail> handleRoomIsFullException(RoomIsFullException ex) {
    return getErrorResponseEntity(BAD_REQUEST, "Room Is Full", ex);
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ProblemDetail> handleUserAlreadyExistsException(
      UserAlreadyExistsException ex) {
    return getErrorResponseEntity(BAD_REQUEST, "User Already Exists", ex);
  }

  @ExceptionHandler(UserAlreadyInRoomException.class)
  public ResponseEntity<ProblemDetail> handleUserAlreadyInRoomException(
      UserAlreadyInRoomException ex) {
    return getErrorResponseEntity(BAD_REQUEST, "User Already In Room", ex);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ProblemDetail> handleBadCredentialsException(BadCredentialsException ex) {
    return getErrorResponseEntity(BAD_REQUEST, "Bad Credentials", ex);
  }

  @ExceptionHandler(AuthorizationDeniedException.class)
  public ResponseEntity<ProblemDetail> handleAuthorizationDeniedException(
      AuthorizationDeniedException ex) {
    return getErrorResponseEntity(FORBIDDEN, "Access Denied", ex);
  }

  @ExceptionHandler(InvalidRoomPasswordException.class)
  public ResponseEntity<ProblemDetail> handleInvalidRoomPasswordException(
      InvalidRoomPasswordException ex) {
    return getErrorResponseEntity(FORBIDDEN, "Invalid Room Password", ex);
  }

  @ExceptionHandler(UserNotAllowedException.class)
  public ResponseEntity<ProblemDetail> handleUserNotAllowedException(UserNotAllowedException ex) {
    return getErrorResponseEntity(FORBIDDEN, "User Not Allowed", ex);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest request) {
    ProblemDetail problemDetail =
        forStatusAndDetail(
            BAD_REQUEST,
            ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(VALIDATION_MESSAGE_DELIMITER)));
    problemDetail.setType(create("validation-error"));
    problemDetail.setTitle("Field Validation Failed");
    return ResponseEntity.status(BAD_REQUEST).body(problemDetail);
  }
}
