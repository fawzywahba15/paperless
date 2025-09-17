#  Paperless Project

Ein verteiltes **Microservice-System** in **Java (Spring Boot)**, vollständig auf **Docker Compose** lauffähig.  
Ziel: Dokumente über eine REST-API hochladen, Metadaten in PostgreSQL speichern, Dateien in MinIO ablegen, Nachrichten über RabbitMQ austauschen, und später Suche via Elasticsearch + KI-Worker.

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
