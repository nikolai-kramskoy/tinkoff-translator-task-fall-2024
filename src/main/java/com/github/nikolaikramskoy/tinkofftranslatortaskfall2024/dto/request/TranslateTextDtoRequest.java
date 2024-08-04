package com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TranslateTextDtoRequest(
    @NotBlank String text, @NotBlank String sourceLanguage, @NotBlank String targetLanguage) {}
