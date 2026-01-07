package org.example.paperlessrest.service;

import org.example.paperlessrest.entity.AccessLog;
import org.example.paperlessrest.entity.Document;
import org.example.paperlessrest.entity.ShareLink;
import org.example.paperlessrest.repository.AccessLogRepository;
import org.example.paperlessrest.repository.DocumentRepository;
import org.example.paperlessrest.repository.ShareLinkRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ShareService {

    private final ShareLinkRepository shareLinkRepository;
    private final AccessLogRepository accessLogRepository;
    private final DocumentRepository documentRepository;

    public ShareService(ShareLinkRepository shareLinkRepository, AccessLogRepository accessLogRepository, DocumentRepository documentRepository) {
        this.shareLinkRepository = shareLinkRepository;
        this.accessLogRepository = accessLogRepository;
        this.documentRepository = documentRepository;
    }

    // 1. Link erstellen (TinyURL generieren)
    public String createShareLink(UUID documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // erzeugen einen kurzen Token (die ersten 8 Zeichen einer UUID) -> "TinyURL"
        String token = UUID.randomUUID().toString().substring(0, 8);

        ShareLink link = ShareLink.builder()
                .document(doc)
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(7)) // Optional: 7 Tage gültig
                .build();

        shareLinkRepository.save(link);
        return token;
    }

    // 2. Link auflösen und Loggen (Tracking)
    public Document getDocumentByToken(String token) {
        ShareLink link = shareLinkRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid Token"));

        boolean isExpired = link.getExpiresAt() != null && link.getExpiresAt().isBefore(LocalDateTime.now());

        // Tracking: speichern JEDEN Zugriff
        AccessLog log = AccessLog.builder()
                .shareLink(link)
                .accessedAt(LocalDateTime.now())
                .successful(!isExpired)
                .build();

        accessLogRepository.save(log);

        if (isExpired) {
            throw new RuntimeException("Link expired");
        }

        return link.getDocument();
    }
}