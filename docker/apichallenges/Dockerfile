# syntax=docker/dockerfile:1

FROM eclipse-temurin:11
WORKDIR /opt/app
COPY ./challenger/target/apichallenges.jar .
CMD ["java", "-jar", "apichallenges.jar"]
EXPOSE 4567