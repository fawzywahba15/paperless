package org.example.paperlessservices.worker;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paperlessservices.dto.xml.XmlAccessLogs;
import org.example.paperlessservices.dto.xml.XmlEntry;
import org.example.paperlessservices.entity.AccessLog;
import org.example.paperlessservices.repository.AccessLogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

/**
 * Worker für den nächtlichen Batch-Import von XML-Access-Logs.
 * Überwacht einen Ordner, liest XML-Dateien ein, speichert Logs in der DB und archiviert die Dateien.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BatchImportWorker {

    private final AccessLogRepository accessLogRepository;
    private final XmlMapper xmlMapper = new XmlMapper();

    @Value("${paperless.batch.input-path:/app/scan_input}")
    private String inputDirPath;

    @Value("${paperless.batch.archive-path:/app/scan_archive}")
    private String archiveDirPath;

    /*
     * -----------------------------------------------------------------------------------
     * SCHEDULER KONFIGURATION
     * -----------------------------------------------------------------------------------
     * Anforderung laut Angabe: Import jede Nacht um 01:00 Uhr.
     * Cron-Expression: "0 0 1 * * *"
     *
     * Für Demo-Zwecke (Präsentation): Import alle 30 Sekunden.
     * FixedRate: 30000 ms
     * -----------------------------------------------------------------------------------
     */

    // AKTIV: DEMO-MODUS (Alle 30 Sekunden)
    @Scheduled(fixedRate = 30000)

    // INAKTIV: PRODUKTIV-MODUS (Täglich um 01:00 Uhr)
    // @Scheduled(cron = "0 0 1 * * *")
    public void processXmlFiles() {
        File inputFolder = new File(inputDirPath);

        if (!inputFolder.exists()) {
            boolean created = inputFolder.mkdirs();
            if(created) log.info("Input-Ordner erstellt: {}", inputDirPath);
        }

        // Filtere nur XML Dateien
        File[] files = inputFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));

        if (files == null || files.length == 0) return;

        log.info("🚀 Batch-Import gestartet: {} XML-Dateien gefunden in {}", files.length, inputDirPath);

        for (File file : files) {
            processSingleFile(file);
        }
    }

    private void processSingleFile(File file) {
        try {
            log.info("   📄 Verarbeite Datei: {}", file.getName());
            XmlAccessLogs logs = xmlMapper.readValue(file, XmlAccessLogs.class);

            if (logs.getEntries() != null) {
                for (XmlEntry entry : logs.getEntries()) {
                    // Wir speichern einen Log-Eintrag.
                    // Hinweis: Da wir im Batch keine ShareLink-ID haben, lassen wir die Relation null.
                    AccessLog dbLog = AccessLog.builder()
                            .accessedAt(LocalDateTime.now())
                            .successful(true)
                            .documentId(entry.getDocumentId())
                            .logMessage("Import User: " + entry.getUser() + " (" + entry.getAccessType() + ")")
                            .build();

                    accessLogRepository.save(dbLog);
                    log.debug("      Eintrag für User '{}' gespeichert (DocID: {})", entry.getUser(), entry.getDocumentId());
                }
                log.info("      ✅ {} Einträge erfolgreich importiert.", logs.getEntries().size());
            }
            moveFileToArchive(file);

        } catch (Exception e) {
            log.error("      ❌ Fehler beim Verarbeiten der Datei {}: {}", file.getName(), e.getMessage());
        }
    }

    private void moveFileToArchive(File file) throws IOException {
        Path archivePath = Paths.get(archiveDirPath);
        if (!Files.exists(archivePath)) Files.createDirectories(archivePath);

        // Timestamp an Dateinamen anhängen, um Überschreiben zu verhindern
        String archivedName = file.getName() + "_" + System.currentTimeMillis() + ".processed";
        Path targetPath = archivePath.resolve(archivedName);

        Files.move(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("      📦 Datei archiviert nach: {}", targetPath);
    }
}