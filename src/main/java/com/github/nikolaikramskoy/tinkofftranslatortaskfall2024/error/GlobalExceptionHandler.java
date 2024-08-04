package com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.error;

import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.error.ErrorDto;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.error.ErrorDtoResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorDtoResponse> handleConstraintViolationException(
      final ConstraintViolationException e) {
    log.info(e.getMessage());

    return ResponseEntity.badRequest()
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            new ErrorDtoResponse(
                e.getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessage)
                    .map(ErrorDto::new)
                    .toList()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorDtoResponse> handleIllegalArgumentException(final Exception e) {
    final var message = e.getMessage();

    log.info(message);

    return ResponseEntity.badRequest()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorDtoResponse(Collections.singletonList(new ErrorDto(message))));
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ErrorDtoResponse> handleException(final Exception e) {
    final var message = e.getMessage();

    log.info(message);

    return ResponseEntity.badRequest()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorDtoResponse(Collections.singletonList(new ErrorDto(message))));
  }
}
