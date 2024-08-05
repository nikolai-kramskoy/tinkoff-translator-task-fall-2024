package com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@AllArgsConstructor
@Getter
public class YandexApiException extends RuntimeException {

  private final HttpStatusCode httpStatusCode;
  private final String message;
}
