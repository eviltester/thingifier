# === Build Stage ===
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom and source code
COPY ../../../pom.xml .
COPY ../../../challenger ./challenger
COPY ../../../challengerAuto ./challengerAuto
COPY ../../../ercoremodel ./ercoremodel
COPY ../../../examplemodels ./examplemodels
COPY ../../../standAloneTodoListManagerRestApi ./standAloneTodoListManagerRestApi
COPY ../../../standAloneTodoListManagerRestApiAuto ./standAloneTodoListManagerRestApiAuto
COPY ../../../standAloneTodoListRestApi ./standAloneTodoListRestApi
COPY ../../../swaggerizer ./swaggerizer
COPY ../../../thingifier ./thingifier
COPY ../../../thingifierapp ./thingifierapp
COPY ../../../todoManagerRestAuto ./todoManagerRestAuto

# Build the application
RUN mvn clean package -DskipTests

# === Runtime Stage ===
FROM openjdk:26-ea-17-jdk-slim

WORKDIR /app

# Copy the jar from the build stage
COPY --from=build /app/challenger/target/apichallenges.jar apichallenges.jar

EXPOSE 4567

# Run the app
ENTRYPOINT ["java", "-jar", "apichallenges.jar", "-model=challengeapi", "-noshutdown", "-multiplayer", "-cloudstorage"]

