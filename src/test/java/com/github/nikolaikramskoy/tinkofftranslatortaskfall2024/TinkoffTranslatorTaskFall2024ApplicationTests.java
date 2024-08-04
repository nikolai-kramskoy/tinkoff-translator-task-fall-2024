package com.github.nikolaikramskoy.tinkofftranslatortaskfall2024;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class TinkoffTranslatorTaskFall2024ApplicationTests {

  @Test
  void contextLoads() {}
}
