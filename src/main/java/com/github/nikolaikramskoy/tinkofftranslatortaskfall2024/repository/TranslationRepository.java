package com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.repository;

import com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.model.Translation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
@Slf4j
public class TranslationRepository {

  private static final String SAVE_TRANSLATION_QUERY =
      "INSERT INTO translation (id, client_ip, \"timestamp\", source_language, "
          + "target_language, text, translated_text) VALUES (:id, :clientIp, "
          + ":timestamp, :sourceLanguage, :targetLanguage, :text, :translatedText)";

  private final JdbcClient jdbcClient;

  public int saveTranslation(final Translation translation) {
    // this is an interesting solution
    // return jdbcClient.sql(SAVE_TRANSLATION_QUERY).paramSource(translation).update();

    log.debug("Trying to insert Translation {} into DB", translation.id());

    return jdbcClient
        .sql(SAVE_TRANSLATION_QUERY)
        .param("id", translation.id())
        .param("clientIp", translation.clientIp())
        .param("timestamp", translation.timestamp())
        .param("sourceLanguage", translation.sourceLanguage())
        .param("targetLanguage", translation.targetLanguage())
        .param("text", translation.text())
        .param("translatedText", translation.translatedText())
        .update();
  }
}
