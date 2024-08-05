package com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.configuration;

import java.time.Clock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ApplicationConfiguration {

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  public ExecutorService yandexTranslateApiExecutorService() {
    return Executors.newFixedThreadPool(10);
  }

  @Bean
  public String yandexApiUrl(@Value("${YANDEX_API_URL}") final String yandexApiUrl) {
    return yandexApiUrl;
  }

  @Bean
  public String yandexApiKey(@Value("${YANDEX_API_KEY}") final String yandexApiKey) {
    return yandexApiKey;
  }
}
