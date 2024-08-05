package com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.error;

import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.error.ErrorDto;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.error.ErrorDtoResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorDtoResponse> handleConstraintViolationException(
      final ConstraintViolationException e) {
    log.debug(e.getMessage());

    return ResponseEntity.badRequest()
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            new ErrorDtoResponse(
                e.getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessage)
                    .map(ErrorDto::new)
                    .toList()));
  }

  @ExceptionHandler(ApplicationException.class)
  public ResponseEntity<ErrorDtoResponse> handleApplicationException(final ApplicationException e) {
    log.info("ApplicationException", e);

    // or maybe 4xx is better...
    return ResponseEntity.internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorDtoResponse(Collections.singletonList(new ErrorDto(e.getMessage()))));
  }

  @ExceptionHandler(YandexApiException.class)
  public ResponseEntity<ErrorDtoResponse> handleYandexApiException(final YandexApiException e) {
    log.info("YandexApiException", e);

    return ResponseEntity.status(e.getHttpStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorDtoResponse(Collections.singletonList(new ErrorDto(e.getMessage()))));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorDtoResponse> handleException(final Exception e) {
    final var message = e.getMessage();

    log.info(message);

    return ResponseEntity.internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorDtoResponse(Collections.singletonList(new ErrorDto(message))));
  }
}
