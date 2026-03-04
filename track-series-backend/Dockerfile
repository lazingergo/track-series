FROM gradle:8.10.2-jdk21 AS build
WORKDIR /app

COPY gradlew gradlew.bat build.gradle settings.gradle lint.gradle checkstyle.xml pmd-ruleset.xml spotbugs-filter.xml ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x ./gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
