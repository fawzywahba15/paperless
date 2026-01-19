package org.example.paperlessservices.worker;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.example.paperlessservices.dto.xml.XmlAccessLogs;
import org.example.paperlessservices.dto.xml.XmlEntry;
import org.example.paperlessservices.entity.AccessLog;
import org.example.paperlessservices.repository.AccessLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Component
public class BatchImportWorker {

    private final Logger log = LoggerFactory.getLogger(BatchImportWorker.class);
    private final AccessLogRepository accessLogRepository;
    private final XmlMapper xmlMapper = new XmlMapper();

    @Value("${paperless.batch.input-path:/app/scan_input}")
    private String inputDirPath;

    @Value("${paperless.batch.archive-path:/app/scan_archive}")
    private String archiveDirPath;

    public BatchImportWorker(AccessLogRepository accessLogRepository) {
        this.accessLogRepository = accessLogRepository;
    }

    // Für Demo: alle 30s. Für Prod: cron = "0 0 1 * * *" (1 AM)
    @Scheduled(fixedRate = 30000)
    public void processXmlFiles() {
        File inputFolder = new File(inputDirPath);

        if (!inputFolder.exists()) {
            inputFolder.mkdirs();
        }

        File[] files = inputFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));

        if (files == null || files.length == 0) return;

        log.info("🚀 Batch: Found {} XML files in {}", files.length, inputDirPath);

        for (File file : files) {
            try {
                log.info("   📄 Processing: {}", file.getName());
                XmlAccessLogs logs = xmlMapper.readValue(file, XmlAccessLogs.class);

                if (logs.getEntries() != null) {
                    for (XmlEntry entry : logs.getEntries()) {
                        AccessLog dbLog = AccessLog.builder()
                                .documentId(entry.getDocumentId())
                                .accessedAt(LocalDateTime.now())
                                .logMessage("Batch Import User: " + entry.getUser())
                                .successful(true)
                                .shareLink(null)
                                .build();

                        accessLogRepository.save(dbLog);
                    }
                    log.info("      ✅ Imported {} entries.", logs.getEntries().size());
                }
                moveFileToArchive(file);

            } catch (Exception e) {
                log.error("      ❌ Error processing file: {}", file.getName(), e);
            }
        }
    }

    private void moveFileToArchive(File file) throws IOException {
        Path archivePath = Paths.get(archiveDirPath);
        if (!Files.exists(archivePath)) Files.createDirectories(archivePath);

        Path targetPath = archivePath.resolve(file.getName() + "_" + System.currentTimeMillis() + ".processed");
        Files.move(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("      📦 Archived to: {}", targetPath);
    }
}