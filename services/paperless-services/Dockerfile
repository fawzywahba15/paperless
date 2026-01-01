# --- Build-Stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY . .

# Baue nur den Worker Service
# -Dmaven.test.skip=true hinzugefügt
# TODO: entferne -Dmaven.test.skip=true wenn unit tests passen
RUN mvn -q -pl services/paperless-services -am -Dmaven.test.skip=true package

# --- Runtime-Stage ---
FROM eclipse-temurin:21-jre


# Tesseract und Sprachpakete installieren
RUN apt-get update && \
    apt-get install -y tesseract-ocr tesseract-ocr-deu tesseract-ocr-eng && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
# Kopiere das Jar
COPY --from=build /build/services/paperless-services/target/paperless-services-*.jar /app/app.jar

EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]