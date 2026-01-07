package org.example.paperlessrest.controller;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.example.paperlessrest.entity.Document;
import org.example.paperlessrest.service.ShareService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.UUID;

@RestController
@RequestMapping("/api/share")
@CrossOrigin(origins = "http://localhost:4200")
public class ShareController {

    private final ShareService shareService;
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    public ShareController(ShareService shareService, MinioClient minioClient) {
        this.shareService = shareService;
        this.minioClient = minioClient;
    }

    // 1. Link erstellen (POST)
    @PostMapping("/{id}")
    public ResponseEntity<String> createShareLink(@PathVariable UUID id) {
        String token = shareService.createShareLink(id);
        // geben die volle URL zurück
        return ResponseEntity.ok("http://localhost:8081/api/share/download/" + token);
    }

    // 2. Datei herunterladen (GET - Öffentlich via Token)
    @GetMapping("/download/{token}")
    public ResponseEntity<InputStreamResource> downloadViaToken(@PathVariable String token) {
        try {
            // Token prüfen und Dokument holen (Tracking passiert im Service)
            Document doc = shareService.getDocumentByToken(token);

            // Datei aus MinIO streamen
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(doc.getObjectKey()) // Pfad zur Datei im Bucket
                            .build()
            );

            String downloadName;
            if (doc.getTitle() != null && !doc.getTitle().isEmpty()) {
                downloadName = doc.getTitle() + ".pdf";
            } else {
                downloadName = doc.getFilename(); // Nimmt den Original-Namen (z.B. "file.pdf")
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadName + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(stream));

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}