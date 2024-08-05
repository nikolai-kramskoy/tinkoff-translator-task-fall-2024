package com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.error;

import lombok.Getter;

@Getter
public class ApplicationException extends RuntimeException {

  private final ApplicationError applicationError;

  public ApplicationException(final ApplicationError applicationError) {
    super(applicationError.getMessage());

    this.applicationError = applicationError;
  }
}
