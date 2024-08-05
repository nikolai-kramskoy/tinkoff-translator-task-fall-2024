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
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.error.YandexApiException;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.model.Translation;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
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
import org.springframework.transaction.annotation.Transactional;
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

  public TranslationService(
      final Clock clock,
      final ExecutorService yandexTranslateApiExecutorService,
      final String yandexApiUrl,
      final String yandexApiKey,
      final RestTemplate restTemplate)
      throws URISyntaxException {
    this.clock = clock;
    this.yandexTranslateApiExecutorService = yandexTranslateApiExecutorService;
    this.yandexApiListLanguagesUri = new URI(yandexApiUrl + "/languages");
    this.yandexApiTranslateUri = new URI(yandexApiUrl + "/translate");
    this.yandexApiAuthorizationHeaderValue = "Api-Key " + yandexApiKey;
    this.restTemplate = restTemplate;
  }

  public AvailableLanguagesDtoResponse getAvailableLanguages() {
    // maybe it's better to cache this response from Yandex API
    // as available languages for translation don't change frequently
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

  // replace with programmatic
  @Transactional
  public TranslateTextDtoResponse translateText(
      final TranslateTextDtoRequest request, final String clientIp) {
    // TODO
    // while and check if exists in DB
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

    return new TranslateTextDtoResponse(translatedText);
  }

  private HttpHeaders createBasicHttpHeadersForYandexApi() {
    final var headers = new HttpHeaders();

    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.set(AUTHORIZATION, yandexApiAuthorizationHeaderValue);

    return headers;
  }

  private String translateText(final TranslateTextDtoRequest request) {
    final var words = request.text().split("\s+");
    final var translationFutures =
        new ArrayList<Future<ResponseEntity<YandexTranslateDtoResponse>>>(words.length);

    for (var i = 0; i < words.length; ++i) {
      translationFutures.add(
          yandexTranslateApiExecutorService.submit(
              createYandexTranslateCallable(request, words[i])));
    }

    for (final var translationFuture : translationFutures) {
      final ResponseEntity<YandexTranslateDtoResponse> response;

      try {
        response = translationFuture.get();
      } catch (final CancellationException e) {
        continue;
      } catch (final InterruptedException e) {
        cancelFutures
        throw new RuntimeException(e);
      } catch (final ExecutionException e) {
        throw new AssertionError("ExecutionException happened, but shouldn't");
      }

      if (!response.getStatusCode().isSameCodeAs(HttpStatus.OK)) {
        final var yandexApiException =
            new YandexApiException(response.getStatusCode(), response.getBody().message());

        log.info("{}", yandexApiException);

        throw yandexApiException;
      }
    }

    final var translatedText = new StringJoiner(" ");

    return translatedText.toString();
  }

  private Callable<ResponseEntity<YandexTranslateDtoResponse>> createYandexTranslateCallable(
      final TranslateTextDtoRequest request, final String word) {
    return () -> {
      // https://yandex.cloud/ru/docs/translate/api-ref/Translation/translate
      return restTemplate.postForEntity(
          yandexApiTranslateUri,
          new HttpEntity<>(
              new YandexTranslateDtoRequest(
                  request.sourceLanguage(),
                  request.targetLanguage(),
                  Collections.singletonList(word)),
              createBasicHttpHeadersForYandexApi()),
          YandexTranslateDtoResponse.class);
    };
  }

  private void cancelFutures(final List<Future<ResponseEntity<YandexTranslateDtoResponse>>> futures) {
    futures.forEach(future -> future.cancel(true));
  }
}
