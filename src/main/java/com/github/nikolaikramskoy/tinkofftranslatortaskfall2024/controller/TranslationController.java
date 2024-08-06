package com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.controller;

import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.request.TranslateTextDtoRequest;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.response.AvailableLanguagesDtoResponse;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.dto.response.TranslateTextDtoResponse;
import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.service.TranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/v1")
@Tag(name = "Translation API", description = "Translate text using Yandex Translate API")
@Validated
@RequiredArgsConstructor
public class TranslationController {

  private final TranslationService translationService;

  @PostMapping(
      path = "/translate",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Translate text",
      description = "Translate text from source language to target language")
  @ApiResponse(responseCode = "200", description = "Successful translation")
  public ResponseEntity<TranslateTextDtoResponse> translateText(
      @Valid @RequestBody final TranslateTextDtoRequest request,
      final HttpServletRequest httpServletRequest) {
    // in case of using some kind of reverse proxy or LB we need to check
    // special HTTP header for real client IP address
    return ResponseEntity.ok(
        translationService.translateText(request, httpServletRequest.getRemoteAddr()));
  }

  @GetMapping(path = "/available-languages", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get languages available for translation")
  @ApiResponse(responseCode = "200", description = "Successful query")
  public ResponseEntity<AvailableLanguagesDtoResponse> getAvailableLanguages() {
    return ResponseEntity.ok(translationService.getAvailableLanguages());
  }
}
