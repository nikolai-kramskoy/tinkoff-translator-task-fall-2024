package com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.request.TranslateTextDtoRequest;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.request.yandextranslate.YandexTranslateTranslateDtoRequest;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.response.AvailableLanguageDto;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.response.yandextranslate.YandexTranslateLanguageDto;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.response.yandextranslate.YandexTranslateListLanguagesResponse;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.response.yandextranslate.YandexTranslateTranslateDtoResponse;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.response.yandextranslate.YandexTranslateTranslationDto;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.error.YandexApiException;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.repository.TranslationRepository;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;

class TranslationServiceTest {

  private static final Clock CLOCK = Clock.systemUTC();

  private static final String YANDEX_TRANSLATE_API_URL = "http://yandex-translate-api.blah";

  private static final URI YANDEX_TRANSLATE_API_LIST_LANGUAGES_URI =
      URI.create(YANDEX_TRANSLATE_API_URL + "/languages");
  private static final URI YANDEX_TRANSLATE_API_TRANSLATE_URI =
      URI.create(YANDEX_TRANSLATE_API_URL + "/translate");

  private static final String YANDEX_API_KEY = "blah";

  private static final ExecutorService YANDEX_TRANSLATE_API_TRANSLATE_EXECUTOR_SERVICE =
      Executors.newFixedThreadPool(10);

  private RestTemplate restTemplateMock;

  private TransactionTemplate transactionTemplateMock;

  private TranslationService translationService;

  @BeforeEach
  public void initMocks() throws URISyntaxException {
    restTemplateMock = mock(RestTemplate.class);
    transactionTemplateMock = mock(TransactionTemplate.class);

    translationService =
        new TranslationService(
            CLOCK,
            YANDEX_TRANSLATE_API_TRANSLATE_EXECUTOR_SERVICE,
            YANDEX_TRANSLATE_API_URL,
            YANDEX_API_KEY,
            restTemplateMock,
            transactionTemplateMock,
            mock(TranslationRepository.class));
  }

  @Test
  void testGetAvailableLanguages_validRequest_returnDtoResponse() {
    when(restTemplateMock.postForEntity(
            eq(YANDEX_TRANSLATE_API_LIST_LANGUAGES_URI),
            // maybe it would be better to construct precise object, but it's
            // time-consuming
            any(HttpEntity.class),
            eq(YandexTranslateListLanguagesResponse.class)))
        .thenReturn(
            ResponseEntity.ok(
                new YandexTranslateListLanguagesResponse(
                    List.of(
                        new YandexTranslateLanguageDto("az", "azərbaycan"),
                        new YandexTranslateLanguageDto("en", "English")),
                    null)));

    final var expectedLanguages =
        List.of(
            new AvailableLanguageDto("en", "English"),
            new AvailableLanguageDto("az", "azərbaycan"));
    final var actual = translationService.getAvailableLanguages();

    Assertions.assertNotNull(actual);
    Assertions.assertNotNull(actual.availableLanguages());
    Assertions.assertEquals(Set.copyOf(expectedLanguages), Set.copyOf(actual.availableLanguages()));
  }

  @Test
  void testGetAvailableLanguages_yandexApiError_throwYandexApiException() {
    final var errorMessage = "some 4xx error message";

    when(restTemplateMock.postForEntity(
            eq(YANDEX_TRANSLATE_API_LIST_LANGUAGES_URI),
            any(HttpEntity.class),
            eq(YandexTranslateListLanguagesResponse.class)))
        .thenReturn(
            ResponseEntity.badRequest()
                .body(new YandexTranslateListLanguagesResponse(null, errorMessage)));

    final var expected = new YandexApiException(BAD_REQUEST, errorMessage);
    final var actual =
        Assertions.assertThrows(
            YandexApiException.class, () -> translationService.getAvailableLanguages());

    Assertions.assertEquals(expected.getHttpStatusCode(), actual.getHttpStatusCode());
    Assertions.assertEquals(expected.getMessage(), actual.getMessage());
  }

  @Test
  void testTranslateText_validRequest_returnDtoResponse() {
    when(restTemplateMock.postForEntity(
            eq(YANDEX_TRANSLATE_API_TRANSLATE_URI),
            // This was tough to mock, order of execution of Callables inside
            // ExecutorService is arbitrary, so I can't just do consecutive thenReturn
            argThat(
                httpEntity ->
                    ((HttpEntity<YandexTranslateTranslateDtoRequest>) httpEntity)
                        .getBody()
                        .texts()
                        .get(0)
                        .equals("привет")),
            eq(YandexTranslateTranslateDtoResponse.class)))
        .thenReturn(
            ResponseEntity.ok(
                new YandexTranslateTranslateDtoResponse(
                    Collections.singletonList(new YandexTranslateTranslationDto("hello")), null)));
    when(restTemplateMock.postForEntity(
            eq(YANDEX_TRANSLATE_API_TRANSLATE_URI),
            argThat(
                httpEntity ->
                    ((HttpEntity<YandexTranslateTranslateDtoRequest>) httpEntity)
                        .getBody()
                        .texts()
                        .get(0)
                        .equals("всем")),
            eq(YandexTranslateTranslateDtoResponse.class)))
        .thenReturn(
            ResponseEntity.ok(
                new YandexTranslateTranslateDtoResponse(
                    Collections.singletonList(new YandexTranslateTranslationDto("everyone")),
                    null)));

    doNothing().when(transactionTemplateMock).executeWithoutResult(any(Consumer.class));

    final var actual =
        translationService.translateText(
            new TranslateTextDtoRequest("  привет   всем", "ru", "en"), "some-IP-address");

    Assertions.assertNotNull(actual);
    Assertions.assertEquals("hello everyone", actual.translatedText());
  }

  @Test
  void testTranslateText_validRequestAndYandexApiError_throwYandexApiException() {
    final var errorMessage = "some 5xx error message";

    when(restTemplateMock.postForEntity(
            eq(YANDEX_TRANSLATE_API_TRANSLATE_URI),
            any(HttpEntity.class),
            eq(YandexTranslateTranslateDtoResponse.class)))
        .thenReturn(
            ResponseEntity.ok(
                new YandexTranslateTranslateDtoResponse(
                    Collections.singletonList(new YandexTranslateTranslationDto("hello")), null)))
        .thenReturn(
            ResponseEntity.badRequest()
                .body(new YandexTranslateTranslateDtoResponse(null, errorMessage)));

    final var expected = new YandexApiException(BAD_REQUEST, errorMessage);
    final var actual =
        Assertions.assertThrows(
            YandexApiException.class,
            () ->
                translationService.translateText(
                    new TranslateTextDtoRequest("  привет   всем", "ru", "en"), "some-IP-address"));

    Assertions.assertEquals(expected.getHttpStatusCode(), actual.getHttpStatusCode());
    Assertions.assertEquals(expected.getMessage(), actual.getMessage());
  }
}
