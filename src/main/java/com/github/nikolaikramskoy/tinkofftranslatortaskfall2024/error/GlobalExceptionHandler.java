package com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.error;

import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.error.ErrorDto;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.error.ErrorDtoResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorDtoResponse> handleMethodArgumentNotValidException(
      final MethodArgumentNotValidException e) {
    log.info(e.getMessage());

    final var errorDtos = new ArrayList<ErrorDto>();

    for (final var objectError : e.getBindingResult().getGlobalErrors()) {
      errorDtos.add(
          new ErrorDto(
              new StringBuilder(objectError.getObjectName())
                  .append(' ')
                  .append(objectError.getDefaultMessage())
                  .toString()));
    }

    for (final var fieldError : e.getBindingResult().getFieldErrors()) {
      errorDtos.add(
          new ErrorDto(
              new StringBuilder(fieldError.getField())
                  .append(' ')
                  .append(fieldError.getDefaultMessage())
                  .toString()));
    }

    return ResponseEntity.badRequest()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorDtoResponse(errorDtos));
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

    // https://yandex.cloud/ru/docs/translate/api-ref/errors-handling
    return ResponseEntity.status(e.getHttpStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorDtoResponse(Collections.singletonList(new ErrorDto(e.getMessage()))));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorDtoResponse> handleException(final Exception e) {
    log.error("Exception", e);

    return ResponseEntity.internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorDtoResponse(Collections.singletonList(new ErrorDto(e.getMessage()))));
  }
}
