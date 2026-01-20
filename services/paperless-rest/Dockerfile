# --- Build-Stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Kopiere gesamten Context (Root), damit Parent-POMs gefunden werden
COPY . .

# Baue NUR das REST-Modul (-pl) und seine Abhängigkeiten (-am)
# Skip Tests im Build, um Zeit zu sparen (Tests laufen ja in der Pipeline/IDE)
RUN mvn -q -pl services/paperless-rest -am package -DskipTests spring-boot:repackage

# --- Runtime-Stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app

# Kopiere das fertige Jar
COPY --from=build /build/services/paperless-rest/target/paperless-rest-*.jar /app/app.jar

EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]