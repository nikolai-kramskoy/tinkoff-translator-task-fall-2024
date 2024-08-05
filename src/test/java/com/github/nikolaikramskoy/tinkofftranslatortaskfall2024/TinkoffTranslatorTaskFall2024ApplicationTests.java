package com.github.nikolaikramskoy.tinkofftranslatortaskfall2024;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class TinkoffTranslatorTaskFall2024ApplicationTests {

  /*
  Ideally I'd need to create various tests (unit and integration) for
  controllers, services and repositories with JUnit 5, Mockito,
  Testcontainers (for PostgreSQL) and WireMock (for mocking Yandex API)
  and probably do manual testing with Insomnia or Postman with real
  Yandex API key...

  I've launched it with Docker without actual Yandex API key, and it seems
  to work correctly (but I do understand that it isn't a significant achievement)
  */

  @Test
  void contextLoads() {}
}
