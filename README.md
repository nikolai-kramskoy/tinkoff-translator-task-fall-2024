# tinkoff-translator-task-fall-2024

## Usage

This server depends on a single PostgreSQL instance and Yandex Translate
API.

### Launch

In the case you're having `.env` file you can simply run this command
(otherwise you can use `-e` option with `docker compose` or some other method
of supplying environment variables):

```shell
docker compose up
```

You can also use `mvn` directly to compile and package it without docker,
but then you will need to provide PostgreSQL instance yourself.

### Environment variables

You can create `.env` file in this directory and place there all the
needed environment variables so docker compose will automatically read
them and inject in the containers.

You need to set these several environment variables:
- `POSTGRES_HOST`
- `POSTGRES_PORT`
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `YANDEX_TRANSLATE_API_URL` - you can find API URL here
[https://yandex.cloud/ru/docs/translate/api-ref/](https://yandex.cloud/ru/docs/translate/api-ref/).
In testing i've used `https://translate.api.cloud.yandex.net/translate/v2`.
- `YANDEX_API_KEY` - read about API key for Yandex here
[https://yandex.cloud/ru/docs/iam/concepts/authorization/api-key](https://yandex.cloud/ru/docs/iam/concepts/authorization/api-key).

You may optionally set these environment variables:
- `JDK_JAVA_OPTIONS` - example: `JDK_JAVA_OPTIONS="-ea -Ddebug"`.
- `CLI_ARGS` - example:
`CLI_ARGS="--logging.level.org.hibernate.SQL=DEBUG --logging.level.org.hibernate.stat=DEBUG"`.

### CLI arguments

These are common useful properties for testing and debugging purposes
(e.g. to show generated SQL by Spring Data JPA, parameters that are used
in the queries and query statistics (by default they are not shown)):
- `logging.level.org.hibernate.SQL=DEBUG`
- `logging.level.org.hibernate.stat=DEBUG`
- `logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE`
- `spring.jpa.properties.hibernate.generate_statistics=true`

## Possible improvements

Now I have only unit tests for controllers and services. Ideally I'd also
need to create unit tests for repositories and integration tests with
Testcontainers (for PostgreSQL) and/or WireMock (for mocking Yandex API),
and probably do testing with real Yandex API key.

I've launched it with Docker without actual Yandex API key, and it seems
to work correctly (I do understand that it isn't a significant achievement).
