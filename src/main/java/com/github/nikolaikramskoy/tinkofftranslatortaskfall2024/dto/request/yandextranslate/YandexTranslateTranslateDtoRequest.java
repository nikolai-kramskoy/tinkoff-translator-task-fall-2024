package com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.request.yandextranslate;

import java.util.List;

// https://yandex.cloud/ru/docs/translate/api-ref/Translation/translate
public record YandexTranslateTranslateDtoRequest(
    String sourceLanguageCode, String targetLanguageCode, List<String> texts) {}
