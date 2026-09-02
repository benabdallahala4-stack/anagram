# Two stages: the JDK and the Gradle cache stay in the builder, so the shipped image carries a JRE
# and one jar and nothing else.

FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /src

# Build files first, so a source-only change does not re-resolve dependencies on every rebuild.
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN ./gradlew --no-daemon --quiet dependencies > /dev/null

COPY src src
RUN ./gradlew --no-daemon --quiet jar \
    && mv build/libs/*.jar /anagram.jar


FROM eclipse-temurin:17-jre-alpine

# The program reads and writes text and the base image defaults to POSIX/ASCII. Java 18+ would
# default to UTF-8 anyway; on 17 it still follows the locale. Passing -Dfile.encoding on the command
# line rather than via JAVA_TOOL_OPTIONS keeps the JVM from printing a "Picked up" line on every run.
ENV LANG=C.UTF-8

# Nothing here needs root.
RUN adduser --system --disabled-password --home /home/anagram anagram
USER anagram
WORKDIR /home/anagram

COPY --from=build /anagram.jar anagram.jar

# Interactive by nature: run with `docker run --rm -it`. Without -i the program sees end of input
# immediately and exits, which is correct behaviour but looks like a crash.
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "/home/anagram/anagram.jar"]
