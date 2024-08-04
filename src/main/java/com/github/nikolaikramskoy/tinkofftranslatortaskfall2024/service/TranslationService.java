package com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.service;

import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.AvailableLanguageDto;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.TranslationDto;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.request.TranslateTextDtoRequest;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.model.Translation;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@AllArgsConstructor
public class TranslationService {

  private final Clock clock;

  private final ExecutorService translationExecutorService;

  private final RestTemplate restTemplate;

  // replace with programmatic
  @Transactional
  public TranslationDto translateText(
      final TranslateTextDtoRequest request, final String clientIp) {
    // TODO
    final var id = UUID.randomUUID();
    final var translatedText = translateText(request);

    final var timestamp = LocalDateTime.now(clock);
    final var translation =
        new Translation(
            id,
            clientIp,
            timestamp,
            request.sourceLanguage(),
            request.targetLanguage(),
            request.text(),
            translatedText);

    return new TranslationDto(translatedText);
  }

  public List<AvailableLanguageDto> getAvailableLanguages() {
    // TODO
    return null;
  }

  private String translateText(final TranslateTextDtoRequest request) {
    // TODO

    final var words = request.text().split("\s+");
    final var translationFutures = new ArrayList<Future<Integer>>(words.length);

    for (final var word : words) {
      // https://yandex.cloud/ru/docs/translate/api-ref/Translation/translate
      //      translationFutures.add(translationExecutorService.submit(() -> {
      //        restTemplate.
      //      }));
    }

    final var translatedText = new StringJoiner(" ");

    return translatedText.toString();
  }
}
