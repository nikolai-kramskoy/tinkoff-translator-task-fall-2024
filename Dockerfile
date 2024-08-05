FROM maven:3-eclipse-temurin-21-alpine AS mvn-build
WORKDIR /translator-app
COPY pom.xml ./
RUN ["mvn", "dependency:go-offline"]

COPY src/ src/
RUN ["mvn", "package", "-DskipTests"]

FROM eclipse-temurin:21-alpine
WORKDIR /translator-app
COPY config/ config/
COPY --from=mvn-build /translator-app/target/*.jar ./translator-app.jar
EXPOSE 8080
RUN addgroup -S translator-app && adduser -S translator-app -G translator-app
ENTRYPOINT ["sh", "-c", "java -jar translator-app.jar ${CLI_ARGS}"]
