import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DocumentService } from '../../core/services/document.service';
import { Document } from '../../core/models/document.model';

@Component({
  selector: 'app-document-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './document-detail.component.html',
  styleUrl: './document-detail.component.css'
})
export class DocumentDetailComponent implements OnInit {
  document?: Document;
  isLoading = true;
  activeTab: 'ocr' | 'summary' = 'summary';

  constructor(
    private route: ActivatedRoute,
    private documentService: DocumentService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.documentService.getDocumentById(id).subscribe({
        next: (doc) => {
          this.document = doc;
          this.isLoading = false;
        },
        error: () => this.isLoading = false
      });
    }
  }

downloadFile(): void {
    if(!this.document) return;

    this.documentService.createShareLink(this.document.id).subscribe(responseUrl => {

       // 1. Token extrahieren
       const parts = responseUrl.split('/');
       const token = parts[parts.length - 1];

       // 2. Saubere relative URL bauen (Nginx leitet das dann korrekt weiter)
       const cleanUrl = `/api/share/download/${token}`;

       window.open(cleanUrl, '_blank');
    });
  }
}
