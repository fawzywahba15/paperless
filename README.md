#  Paperless Project

Ein verteiltes **Microservice-System** in **Java (Spring Boot)**, vollständig auf **Docker Compose** lauffähig.  
Ziel: Dokumente über eine REST-API hochladen, Metadaten in PostgreSQL speichern, Dateien in MinIO ablegen, Nachrichten über RabbitMQ austauschen, und später Suche via Elasticsearch + KI-Worker.

---
## Dienste & Ports
- paperless-rest (8081), paperless-service (8082), paperless-web (8080/nginx)
- postgres (5432), rabbitmq (5672 + UI 9093), minio (9000 + UI 9090), elasticsearch (9200), adminer (9091)
---
## Start/Stopp
```bash
docker compose --env-file .env up -d          # alle
docker compose --env-file .env up -d paperless-rest
docker compose --env-file .env up -d paperless-service
docker compose --env-file .env stop
docker compose logs -f paperless-rest
```
---
##  Architektur

- **paperless-rest** (Spring Boot, Port 8081)
  - REST API für Upload/Download
  - Speichert Metadaten in PostgreSQL
  - Schickt Files an MinIO
  - Sendet Events an RabbitMQ

- **paperless-service** (Spring Boot, Port 8082)
  - Worker-Service für OCR & KI
  - Liest aus RabbitMQ Queues
  - Schreibt Ergebnisse zurück

- **paperless-web** (Frontend via Nginx, Port 8080)
  - Einfaches UI, spricht mit REST API

- **postgres** (Port 5432) → Datenbank
- **rabbitmq** (5672 + Console 9093) → Messaging
- **minio** (9000 + Console 9090) → File Storage
- **elasticsearch** (9200) → Suche
- **adminer** (9091) → DB Management

## Einzelne Services starten

```bash
docker compose --env-file .env up -d paperless-rest
docker compose --env-file .env up -d paperless-service
docker compose --env-file .env up -d paperless-web
```
---


## Logs ansehen

```bash
docker compose logs -f paperless-rest
docker compose logs -f paperless-service

```
---

## Rebuild

```bash
docker compose --env-file .env build paperless-rest
docker compose --env-file .env build paperless-service


```
---

##  Starten

### Alles starten
```bash
docker compose --env-file .env up -d
```
---


## Fertig:

- Multi-Service Setup via Docker Compose
- Spring Boot REST API (paperless-rest) mit DB, MinIO, RabbitMQ
- Spring Boot Worker-Service (paperless-service)
- OOP-Struktur (Controller, Service, Repository, DTOs, Configs)
- .env → automatische Variablenübergabe

---

## Ablauf (vereinfacht)


- User lädt ein Dokument hoch → paperless-rest empfängt es
- File wird nach MinIO gespeichert
- Metadaten landen in Postgres
- Nachricht „OCR erforderlich“ wird in RabbitMQ Queue gelegt
- paperless-service verarbeitet das Dokument (OCR/KI)
- Ergebnisse werden zurück an REST geschickt / in DB gespeichert
- Elasticsearch dient zur späteren Suche

## sprint 1  unit tests nachweisen

```bash
cd C:\Users\fawzi\Desktop\paperless\services\paperless-rest
docker run --rm -v "${PWD}:/build" -w /build maven:3.9-eclipse-temurin-21 `
  mvn -pl services/paperless-rest -am test

```

## sprint 2  unit tests nachweisen

```bash
# Images bauen (Docker erstellt die App-Container-Images neu)
docker compose --env-file .env build paperless-rest
docker compose --env-file .env build paperless-service

# Stack starten (alle nötigen Container aus docker-compose.yml)
docker compose --env-file .env up -d



# Datei hochladen (Multipart/Form-Data, Feldname "file")
curl -F "C:\Users\fawzi\Downloads\Lebenslauf.pdf" http://localhost:8081/api/documents

# Status abfragen (Polling)
curl http://localhost:8081/api/documents/<ID>

```

## sprint 3  nachweis

```bash
# Images bauen (Docker erstellt die App-Container-Images neu)
docker compose --env-file .env build paperless-rest
docker compose --env-file .env build paperless-service

# Stack starten (alle nötigen Container aus docker-compose.yml)
docker compose --env-file .env up -d



# Datei hochladen in cmd

curl -X POST http://localhost:8081/api/documents ^
     -F "file=@C:/Users/Fawzy/Downloads/test.pdf"

# Datei hochladen in IDE

& "C:\Windows\System32\curl.exe" -X POST http://localhost:8081/api/documents -F "file=@C:/Users/Fawzy/Downloads/test.pdf"




# Status abfragen (Polling)
curl http://localhost:8081/api/documents/<ID>

```
