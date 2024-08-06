package com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.configuration;

import java.time.Clock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfiguration {

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  public ExecutorService yandexTranslateApiTranslateExecutorService() {
    return Executors.newFixedThreadPool(10);
  }

  @Bean
  public String yandexTranslateApiUrl(
      @Value("${YANDEX_TRANSLATE_API_URL}") final String yandexTranslateApiUrl) {
    return yandexTranslateApiUrl;
  }

  @Bean
  public String yandexApiKey(@Value("${YANDEX_API_KEY}") final String yandexApiKey) {
    return yandexApiKey;
  }

  @Bean
  public RestTemplate restTemplate(final RestTemplateBuilder builder) {
    return builder.build();
  }
}
