# --- Build-Stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
# Gesamtes Repo kopieren (damit Parent-POM vorhanden ist)
COPY . .
# Baue NUR das Modul paperless-services und dessen Abhängigkeiten
RUN mvn -q -DskipTests -pl services/paperless-services -am package

# --- Runtime-Stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app
# Kopiere das Boot-REPACKAGE-Artifact (hat Main-Manifest)
COPY --from=build /build/services/paperless-services/target/paperless-services-*.jar /app/app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
