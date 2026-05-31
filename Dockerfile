FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /build
COPY gradle gradle
COPY gradlew .
COPY settings.gradle .
COPY build.gradle .
RUN ./gradlew --version
RUN ./gradlew dependencies --no-daemon
COPY src/main/resources/openapi.yaml src/main/resources/openapi.yaml
RUN ./gradlew openApiGenerate --no-daemon
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-jammy AS runner
WORKDIR /app
RUN useradd -m springuser
USER springuser
COPY --from=builder /build/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dfile.encoding=UTF-8", "app.jar"]