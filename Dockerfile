ARG VERSION=8u171

# Step 1: Build image
FROM openjdk:${VERSION}-jdk-alpine as BUILD

COPY . /src
WORKDIR /src
RUN ./gradlew --no-daemon shadowJar


# Step 2: Standalone image
FROM openjdk:${VERSION}-jre-alpine

COPY --from=BUILD /src/build/libs/ /svc/
WORKDIR /svc

CMD ["java", "-jar", "poe-public-stash-downloader.jar"]