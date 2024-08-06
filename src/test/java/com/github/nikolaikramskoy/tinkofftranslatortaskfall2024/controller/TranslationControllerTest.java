package com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.controller.utility.JsonConverter;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.error.ErrorDtoResponse;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.request.TranslateTextDtoRequest;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.response.AvailableLanguageDto;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.response.AvailableLanguagesDtoResponse;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.response.TranslateTextDtoResponse;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.service.TranslationService;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TranslationController.class)
class TranslationControllerTest {

  private static final String GET_AVAILABLE_LANGUAGES_API_URL = "/v1/available-languages";
  private static final String TRANSLATE_TEXT_API_URL = "/v1/translate";

  @Autowired private MockMvc mockMvc;

  @MockBean private TranslationService translationServiceMock;

  @Test
  void testGetAvailableLanguages_validRequest_returnDtoResponse() throws Exception {
    final var availableLanguageDtos =
        Collections.singletonList(new AvailableLanguageDto("en", "English"));

    when(translationServiceMock.getAvailableLanguages())
        .thenReturn(new AvailableLanguagesDtoResponse(availableLanguageDtos));

    final var mvcResult =
        mockMvc
            .perform(get(GET_AVAILABLE_LANGUAGES_API_URL).characterEncoding(StandardCharsets.UTF_8))
            .andReturn();

    final var response = mvcResult.getResponse();

    Assertions.assertEquals(200, response.getStatus());

    final var actual =
        JsonConverter.jsonToObject(
            response.getContentAsString(), AvailableLanguagesDtoResponse.class);

    Assertions.assertEquals(
        Set.copyOf(availableLanguageDtos), Set.copyOf(actual.availableLanguages()));
  }

  @Test
  void testTranslateText_validRequest_returnDtoResponse() throws Exception {
    final var request = new TranslateTextDtoRequest("  привет  всем", "ru", "en");
    final var translatedText = "hello everyone";

    when(translationServiceMock.translateText(eq(request), any(String.class)))
        .thenReturn(new TranslateTextDtoResponse(translatedText));

    final var mvcResult =
        mockMvc
            .perform(
                post(TRANSLATE_TEXT_API_URL)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonConverter.objectToJson(request)))
            .andReturn();

    final var response = mvcResult.getResponse();

    Assertions.assertEquals(200, response.getStatus());

    final var actual =
        JsonConverter.jsonToObject(response.getContentAsString(), TranslateTextDtoResponse.class);

    Assertions.assertEquals(translatedText, actual.translatedText());
  }

  @Test
  void testTranslateText_invalidRequest_returnErrorDtoResponse() throws Exception {
    final var request = new TranslateTextDtoRequest("", "", "");

    final var mvcResult =
        mockMvc
            .perform(
                post(TRANSLATE_TEXT_API_URL)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonConverter.objectToJson(request)))
            .andReturn();

    final var response = mvcResult.getResponse();

    Assertions.assertEquals(400, response.getStatus());

    final var errorDtoResponse =
        JsonConverter.jsonToObject(response.getContentAsString(), ErrorDtoResponse.class);

    // Must be 3 not blank validation errors
    Assertions.assertEquals(3, errorDtoResponse.errors().size());
  }
}
