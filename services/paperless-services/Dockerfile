# --- Build-Stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Kopiere gesamten Context
COPY . .

# Baue NUR das Services-Modul
RUN mvn -q -pl services/paperless-services -am package -DskipTests spring-boot:repackage

# --- Runtime-Stage ---
FROM eclipse-temurin:21-jre

# Tesseract OCR und Sprachpakete installieren (WICHTIG für Worker!)
RUN apt-get update && \
    apt-get install -y tesseract-ocr tesseract-ocr-deu tesseract-ocr-eng && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /build/services/paperless-services/target/paperless-services-*.jar /app/app.jar

# Ordner für Batch-Import anlegen (wird aber durch Volume überschrieben)
RUN mkdir -p /app/scan_input /app/scan_archive

EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]