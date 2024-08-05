package com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.response.yandextranslate;

import java.util.List;

// message is for possible error
public record YandexListLanguagesResponse(List<YandexLanguageDto> languages, String message) {}
