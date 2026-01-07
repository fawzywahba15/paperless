# Paperless - Verteiltes Dokumentenmanagementsystem

Ein modernes, Microservice-basiertes System zur Archivierung, Verarbeitung und Suche von Dokumenten. Entwickelt im Rahmen des Semesterprojekts (Sprints 1-6).

Das System ermöglicht den Upload von PDFs, extrahiert Text mittels OCR (Tesseract), generiert KI-Zusammenfassungen (Google Gemini), speichert Daten relational (PostgreSQL) sowie als Objekte (MinIO) und ermöglicht eine performante Volltextsuche (ElasticSearch).

---

## 🚀 Features & Highlights

Das Projekt deckt alle Anforderungen der Sprints 1 bis 6 ab:

* **📄 Dokumenten-Upload:**
    * Upload via Web-UI (Angular) oder REST API.
    * Speicherung der Originaldatei in **MinIO** (S3-kompatibler Object Storage).
    * Speicherung der Metadaten in **PostgreSQL**.
* **⚙️ Asynchrone Verarbeitung (Messaging):**
    * Nutzung von **RabbitMQ** zur Entkopplung von Upload und Verarbeitung.
    * Skalierbare Worker-Architektur.
* **🔍 OCR & Texterkennung:**
    * Automatisierte Textextraktion mit **Tesseract OCR**.
* **🤖 KI-Integration (GenAI):**
    * Automatische Zusammenfassung des Dokumenteninhalts mittels **Google Gemini API**.
* **🔎 Intelligente Suche (ElasticSearch):**
    * Indizierung aller Dokumenteninhalte.
    * Fuzzy-Search (unscharfe Suche) über Titel und OCR-Content.
* **🔗 Secure Document Sharing (Additional Use-Case):**
    * Erstellen von zeitlich begrenzten Share-Links.
    * Tracking von Zugriffen (Access Logs) in der Datenbank.
* **📚 API Dokumentation:**
    * Vollständige OpenAPI/Swagger Spezifikation integriert.

---

## 🛠️ Technologie-Stack

### Backend
* **Framework:** Spring Boot 3 (Java 21)
* **Kommunikation:** REST, AMQP (RabbitMQ)
* **Datenbank:** PostgreSQL 16 (Hibernate/JPA)
* **Search Engine:** ElasticSearch 8
* **Object Storage:** MinIO
* **Tools:** Lombok, MapStruct (Entity-DTO Mapping), Maven

### Frontend
* **Framework:** Angular 18 (Standalone Components)
* **Styling:** Custom CSS, Responsive Layout
* **Server:** Nginx (als Reverse Proxy und Webserver)

### Infrastruktur
* **Containerisierung:** Docker & Docker Compose
* **Orchestrierung:** Alle Services starten mit einem Befehl.

---

## 🏃‍♂️ Installation & Start

### Voraussetzungen
* Docker & Docker Desktop installiert.
* Git installiert.
* (Optional) Java 21 JDK & Maven für lokale Entwicklung.

### Starten der Anwendung
Das gesamte System wird über Docker Compose gestartet. Es ist keine lokale Installation von Datenbanken oder Java notwendig.

1.  **Repository klonen:**
    ```bash
    git clone https://github.com/fawzywahba15/paperless
    cd paperless
    ```

2.  **System bauen und starten:**
    Dieser Befehl baut die Java-JARs, die Angular-App und erstellt die Docker-Images.
    ```bash
    docker-compose up --build
    ```
    *(Der erste Start kann einige Minuten dauern, da Maven-Abhängigkeiten und Docker-Images geladen werden.)*

3.  **Warten auf Bereitschaft:**
    Warten Sie, bis in den Logs `Started PaperlessRestApplication` und `Started PaperlessServiceApplication` erscheint.

---

## 🖥️ Nutzung & Zugriff

Nach dem erfolgreichen Start sind folgende Dienste erreichbar:

| Dienst | URL | Beschreibung |
| :--- | :--- | :--- |
| **Web UI** | [http://localhost:8080](http://localhost:8080) | Die Hauptanwendung für Benutzer. |
| **API Docs** | [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html) | Swagger UI zum Testen der Endpunkte. |
| **MinIO** | [http://localhost:9090](http://localhost:9090) | S3 Konsole. |
| **RabbitMQ** | [http://localhost:9093](http://localhost:9093) | Messaging Dashboard. |
| **Adminer** | [http://localhost:9091](http://localhost:9091) | Datenbank-Verwaltung. |
### 🔑 Standard-Zugangsdaten

Damit Sie sich direkt in die Verwaltungsoberflächen einloggen können:

| Dienst | Benutzer / Login | Passwort | Zusatzinfo |
| :--- | :--- | :--- | :--- |
| **Adminer (DB)** | `paperless` | `paperless` | System: **PostgreSQL**, DB: `paperless` |
| **MinIO** | `minioadmin` | `minioadmin` | |
| **RabbitMQ** | `guest` | `guest` | |

---

### Workflow testen (Der "Happy Path")

1.  Öffnen Sie das **Web UI** (`http://localhost:8080`).
2.  Laden Sie ein PDF-Dokument über das Upload-Formular hoch.
3.  **Warten:** Im Hintergrund passiert folgendes:
    * REST-API speichert Datei in MinIO & DB.
    * RabbitMQ Nachricht -> OCR Worker extrahiert Text.
    * GenAI Worker -> Erstellt Zusammenfassung.
    * Indexer -> Speichert Text in ElasticSearch.
4.  Nutzen Sie die **Suchleiste**: Suchen Sie nach einem Wort, das *im* PDF vorkommt (z.B. "Rechnung", "Wien"). Das Dokument sollte erscheinen.
5.  Klicken Sie auf **"Share"**, kopieren Sie den Link und testen Sie den Download in einem neuen Tab.

---

## ✅ Qualitätssicherung & Unit-Tests

Das Projekt verfügt über eine umfassende Test-Suite, die kritische Pfade und Business-Logik absichert (Code Coverage > 70%).

### Was wird getestet?
* **Controller Layer:** `DocumentControllerTest` prüft HTTP-Endpunkte und Status-Codes.
* **Service Layer:** `DocumentServiceTest` und `ShareServiceTest` validieren die Geschäftslogik (Upload, Sharing, Logging).
* [cite_start]**Worker Services:** `OcrConsumerTest` und `GenAiConsumerTest` simulieren die asynchrone Verarbeitung (RabbitMQ, MinIO, External APIs).

### Tests ausführen

**Option 1: Automatisch im Docker Build**
Die Tests werden automatisch ausgeführt, wenn der Container gebaut wird. Ein erfolgreicher Start (`docker-compose up --build`) bestätigt, dass alle Tests grün sind.

**Option 2: Manuell (Lokal)**
Sie können die Tests auch ohne Docker direkt mit Maven ausführen:

```bash
# Beispiel für REST Service
cd services/paperless-rest
./mvnw test

# Beispiel für Worker Service
cd services/paperless-services
./mvnw test
```

---

## 🧪 Additional Use-Case: Document Sharing

Dieser Use-Case (Sprint 6) demonstriert die Erweiterung des Datenmodells und der Business-Logik.

* **Funktion:** Benutzer können Dokumente temporär freigeben.
* **Neue Entities:**
    * `ShareLink`: Speichert Token und Ablaufdatum.
    * `AccessLog`: Protokolliert jeden Download-Versuch (Erfolg/Fehler, Zeitstempel).
* **Testen:**
    1.  Klicken Sie im UI auf den blauen "Share" Button bei einem Dokument.
    2.  Ein Link wird generiert (z.B. `http://localhost:8081/api/share/download/xyz123`).
    3.  Beim Aufruf dieses Links wird das Dokument direkt heruntergeladen.
    4.  Prüfung: In der Datenbank-Tabelle `access_log` ist nun ein Eintrag sichtbar.

---

## ⚠️ Troubleshooting

1. Upload hängt? 
* Prüfen Sie die Logs (docker-compose logs -f paperless-service), ob Tesseract OCR läuft.

2. Suche findet nichts? 
* Warten Sie einige Sekunden nach dem Upload (ElasticSearch Indexierung ist asynchron).

3. Datenbank-Fehler? 
* Führen Sie docker-compose down -v aus, um die Volumes zu löschen und mit einer frischen DB zu starten.

## 📂 Projektstruktur

```plaintext
paperless/
├── compose/                # Konfigurationsdateien für Infrastruktur (DB, Nginx, ES)
├── services/
│   ├── paperless-rest/     # Spring Boot Backend (API, Controller, Entities)
│   ├── paperless-services/ # Spring Boot Worker (OCR, Search, GenAI Listener)
│   └── paperless-web/      # Angular Frontend
├── docker-compose.yml      # Orchestrierung aller Container
└── README.md               # Diese Datei

