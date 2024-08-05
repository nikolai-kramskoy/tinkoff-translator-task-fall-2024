package com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ApplicationError {
  TIMEOUT("Timeout, try again later.");

  private final String message;
}
