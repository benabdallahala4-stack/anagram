FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /src

# Build files first so dependency resolution is cached across source changes.
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN ./gradlew --no-daemon --quiet dependencies > /dev/null

COPY src src
RUN ./gradlew --no-daemon --quiet jar \
    && mv build/libs/*.jar /anagram.jar


FROM eclipse-temurin:17-jre-alpine

ENV LANG=C.UTF-8

RUN adduser --system --disabled-password --home /home/anagram anagram
USER anagram
WORKDIR /home/anagram

COPY --from=build /anagram.jar anagram.jar

# Run with `docker run --rm -it`; the program reads stdin.
# -Dfile.encoding on the command line rather than JAVA_TOOL_OPTIONS avoids the "Picked up" noise.
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "/home/anagram/anagram.jar"]
