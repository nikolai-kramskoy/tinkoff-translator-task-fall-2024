package com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record Translation(
    UUID id,
    String clientIp,
    LocalDateTime timestamp,
    String sourceLanguage,
    String targetLanguage,
    String text,
    String translatedText) {}
