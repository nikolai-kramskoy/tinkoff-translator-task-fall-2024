package com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.service;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.EmptyDto;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.request.TranslateTextDtoRequest;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.request.yandextranslate.YandexTranslateDtoRequest;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.response.AvailableLanguageDto;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.response.AvailableLanguagesDtoResponse;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.response.TranslateTextDtoResponse;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.response.yandextranslate.YandexListLanguagesResponse;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.response.yandextranslate.YandexTranslateDtoResponse;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.error.ApplicationError;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.error.ApplicationException;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.error.YandexApiException;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.model.Translation;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.repository.TranslationRepository;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class TranslationService {

  private final Clock clock;

  private final ExecutorService yandexTranslateApiExecutorService;

  private final URI yandexApiListLanguagesUri;
  private final URI yandexApiTranslateUri;

  private final String yandexApiAuthorizationHeaderValue;

  private final RestTemplate restTemplate;

  private final TransactionTemplate transactionTemplate;

  private final TranslationRepository translationRepository;

  public TranslationService(
      final Clock clock,
      final ExecutorService yandexTranslateApiExecutorService,
      final String yandexApiUrl,
      final String yandexApiKey,
      final RestTemplate restTemplate,
      final TransactionTemplate transactionTemplate,
      final TranslationRepository translationRepository)
      throws URISyntaxException {
    this.clock = clock;
    this.yandexTranslateApiExecutorService = yandexTranslateApiExecutorService;
    this.yandexApiListLanguagesUri = new URI(yandexApiUrl + "/languages");
    this.yandexApiTranslateUri = new URI(yandexApiUrl + "/translate");
    this.yandexApiAuthorizationHeaderValue = "Api-Key " + yandexApiKey;
    this.restTemplate = restTemplate;
    this.transactionTemplate = transactionTemplate;
    this.translationRepository = translationRepository;
  }

  public AvailableLanguagesDtoResponse getAvailableLanguages() {
    log.debug("query Yandex API for available languages");

    // maybe it's better to cache this response from Yandex API
    // as available languages for translation don't change frequently
    // (e.g. at application start)

    // https://yandex.cloud/ru/docs/translate/api-ref/Translation/listLanguages
    final var response =
        restTemplate.postForEntity(
            yandexApiListLanguagesUri,
            new HttpEntity<>(new EmptyDto(), createBasicHttpHeadersForYandexApi()),
            YandexListLanguagesResponse.class);

    if (!response.getStatusCode().isSameCodeAs(HttpStatus.OK)) {
      throw new YandexApiException(response.getStatusCode(), response.getBody().message());
    }

    return new AvailableLanguagesDtoResponse(
        response.getBody().languages().stream()
            .map(language -> new AvailableLanguageDto(language.code(), language.name()))
            .toList());
  }

  public TranslateTextDtoResponse translateText(
      final TranslateTextDtoRequest request, final String clientIp) {
    log.info("translate text {} for client with IP {}", request, clientIp);

    final var translatedText = translateText(request);
    final var timestamp = LocalDateTime.now(clock);

    transactionTemplate.executeWithoutResult(
        transactionStatus -> {
          var translation =
              new Translation(
                  UUID.randomUUID(),
                  clientIp,
                  timestamp,
                  request.sourceLanguage(),
                  request.targetLanguage(),
                  request.text(),
                  translatedText);

          // on successful update saveTranslation must return 1, if it's 0, then
          // it's a rare case that generated random UUID exists in the table
          while (translationRepository.saveTranslation(translation) == 0) {
            translation =
                new Translation(
                    UUID.randomUUID(),
                    clientIp,
                    timestamp,
                    request.sourceLanguage(),
                    request.targetLanguage(),
                    request.text(),
                    translatedText);
          }

          log.info("successful insertion of translation {} in the DB", translation.id());
        });

    return new TranslateTextDtoResponse(translatedText);
  }

  private HttpHeaders createBasicHttpHeadersForYandexApi() {
    final var headers = new HttpHeaders();

    headers.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.set(AUTHORIZATION, yandexApiAuthorizationHeaderValue);
    headers.setContentType(MediaType.APPLICATION_JSON);

    return headers;
  }

  private String translateText(final TranslateTextDtoRequest request) {
    final var words = request.text().split("\s+");
    final var translationFutures =
        new ArrayList<Future<ResponseEntity<YandexTranslateDtoResponse>>>(words.length);

    for (var i = 0; i < words.length; ++i) {
      translationFutures.add(
          yandexTranslateApiExecutorService.submit(
              createYandexTranslateCallableRequest(request, words[i])));
    }

    final var translatedText = new StringJoiner(" ");

    for (final var translationFuture : translationFutures) {
      final ResponseEntity<YandexTranslateDtoResponse> response;

      try {
        // Probably setting some timeout value would be better
        response = translationFuture.get();
      } catch (final InterruptedException e) {
        // maybe I should've try to wait more and then cancel the operations
        cancelFutures(translationFutures);

        // AFAIK I need to re-interrupt current thread before throwing exception
        Thread.currentThread().interrupt();

        throw new ApplicationException(ApplicationError.TIMEOUT);
      } catch (final ExecutionException e) {
        throw new AssertionError("ExecutionException happened, but shouldn't");
      }

      if (!response.getStatusCode().isSameCodeAs(HttpStatus.OK)) {
        cancelFutures(translationFutures);

        throw new YandexApiException(response.getStatusCode(), response.getBody().message());
      }

      // I submit only one word, so I will get only one translated word
      translatedText.add(response.getBody().translations().get(0).text());
    }

    return translatedText.toString();
  }

  private Callable<ResponseEntity<YandexTranslateDtoResponse>> createYandexTranslateCallableRequest(
      final TranslateTextDtoRequest request, final String word) {
    return () ->
        // https://yandex.cloud/ru/docs/translate/api-ref/Translation/translate
        restTemplate.postForEntity(
            yandexApiTranslateUri,
            new HttpEntity<>(
                new YandexTranslateDtoRequest(
                    request.sourceLanguage(),
                    request.targetLanguage(),
                    Collections.singletonList(word)),
                createBasicHttpHeadersForYandexApi()),
            YandexTranslateDtoResponse.class);
  }

  // It's best to cancel pending futures if some of them failed
  private void cancelFutures(
      final List<Future<ResponseEntity<YandexTranslateDtoResponse>>> futures) {
    futures.forEach(future -> future.cancel(true));
  }
}
