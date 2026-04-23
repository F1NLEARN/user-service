FROM eclipse-temurin:17-jdk-alpine AS common-builder
WORKDIR /workspace

COPY common/gradle gradle
COPY common/gradlew gradlew
COPY common/build.gradle build.gradle
COPY common/settings.gradle settings.gradle
COPY common/src src

RUN chmod +x gradlew && ./gradlew publishToMavenLocal -x test --no-daemon

FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /workspace

COPY --from=common-builder /root/.m2 /root/.m2

COPY user-service/gradle gradle
COPY user-service/gradlew gradlew
COPY user-service/build.gradle build.gradle
COPY user-service/settings.gradle settings.gradle
COPY user-service/src src

RUN chmod +x gradlew && ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]