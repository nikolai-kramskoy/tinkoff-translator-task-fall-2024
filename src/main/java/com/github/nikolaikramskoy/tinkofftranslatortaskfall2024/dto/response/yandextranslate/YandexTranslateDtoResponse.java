package com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.response.yandextranslate;

import java.util.List;

// message is for possible error
public record YandexTranslateDtoResponse(List<YandexTranslationDto> translations, String message) {}
