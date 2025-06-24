package me.civka.monopoly.util;

import static java.net.URI.create;
import static org.springframework.http.ProblemDetail.forStatusAndDetail;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

public class ErrorUtils {

  public static final String TITLE_SPACE_SEPARATOR = " ";
  public static final String TITLE_DASH_SEPARATOR = "-";

  public static ProblemDetail getError(HttpStatus status, String title, Exception ex) {
    ProblemDetail problemDetail = forStatusAndDetail(status, ex.getMessage());
    problemDetail.setType(
        create(title.toLowerCase().replaceAll(TITLE_SPACE_SEPARATOR, TITLE_DASH_SEPARATOR)));
    problemDetail.setTitle(title);
    return problemDetail;
  }

  public static ResponseEntity<ProblemDetail> getErrorResponseEntity(
      HttpStatus status, String title, Exception ex) {
    return ResponseEntity.status(status).body(getError(status, title, ex));
  }
}
