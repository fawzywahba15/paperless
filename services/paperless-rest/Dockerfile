# --- Build-Stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# 1) Nur POM kopieren, Dependencies cachen
COPY services/paperless-rest/pom.xml services/paperless-rest/
RUN mvn -q -f services/paperless-rest/pom.xml -DskipTests dependency:go-offline

# 2) Source kopieren und bauen (inkl. repackage)
COPY services/paperless-rest/ services/paperless-rest/
RUN mvn -q -f services/paperless-rest/pom.xml -DskipTests package spring-boot:repackage

# --- Runtime-Stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app
# Robust: irgendein erzeugtes Jar nehmen (ohne -plain)
ARG JAR_FILE=services/paperless-rest/target/*.jar
COPY --from=build /build/${JAR_FILE} /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
