package com.github.nikolaikramskoy.tinkofftranslatortaskfall2024.controller.utility;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;

public class JsonConverter {

  private static final ObjectMapper objectMapper =
      new ObjectMapper()
          .setSerializationInclusion(JsonInclude.Include.NON_NULL)
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .findAndRegisterModules();

  private JsonConverter() {}

  public static String objectToJson(final Object obj) throws IOException {
    return objectMapper.writeValueAsString(obj);
  }

  public static <T> T jsonToObject(final String json, final Class<T> clazz) throws IOException {
    return objectMapper.readValue(json, clazz);
  }

  public static <T> List<T> jsonToListOfObjects(final String json, final Class<T> clazz)
      throws IOException {
    return objectMapper.readerForListOf(clazz).readValue(json);
  }
}
