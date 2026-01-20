package org.example.paperlessrest.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paperlessrest.entity.AccessLog;
import org.example.paperlessrest.entity.Document;
import org.example.paperlessrest.entity.ShareLink;
import org.example.paperlessrest.repository.AccessLogRepository;
import org.example.paperlessrest.repository.DocumentRepository;
import org.example.paperlessrest.repository.ShareLinkRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service für die Logik hinter Freigabe-Links.
 * Handhabt Erstellung, Validierung, Access-Logging (Sprint 7) und MinIO-Zugriff.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShareService {

    private final ShareLinkRepository shareLinkRepository;
    private final AccessLogRepository accessLogRepository;
    private final DocumentRepository documentRepository;
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    /**
     * Erstellt einen neuen Share-Link für 7 Tage.
     */
    public String createShareLink(UUID documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Dokument nicht gefunden"));

        // Generiere "TinyURL" Token (erste 8 Zeichen einer UUID)
        String token = UUID.randomUUID().toString().substring(0, 8);

        ShareLink link = ShareLink.builder()
                .document(doc)
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        shareLinkRepository.save(link);
        log.info("Share-Link erstellt: {} (gültig bis {})", token, link.getExpiresAt());
        return token;
    }

    /**
     * Validiert ein Token, protokolliert den Zugriff (AccessLog) und gibt das Dokument zurück.
     * Wirft eine Exception, wenn Token ungültig oder abgelaufen.
     */
    public Document getDocumentByToken(String token) {
        ShareLink link = shareLinkRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Ungültiger Token"));

        boolean isExpired = link.getExpiresAt() != null && link.getExpiresAt().isBefore(LocalDateTime.now());

        // Access Logging (Anforderung Sprint 7)
        AccessLog logEntry = AccessLog.builder()
                .shareLink(link)
                .accessedAt(LocalDateTime.now())
                .successful(!isExpired)
                .build();

        accessLogRepository.save(logEntry);
        log.info("Zugriff auf Token {} protokolliert. Erfolgreich: {}", token, !isExpired);

        if (isExpired) {
            throw new RuntimeException("Link ist abgelaufen");
        }

        return link.getDocument();
    }

    /**
     * Lädt den physischen Datei-Stream aus dem MinIO Object Storage.
     */
    public InputStream getFileStream(Document doc) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(doc.getObjectKey())
                        .build()
        );
    }
}