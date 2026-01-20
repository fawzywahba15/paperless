import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DocumentService } from '../../core/services/document.service';
import { Document } from '../../core/models/document.model';

@Component({
  selector: 'app-document-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './document-list.component.html',
  styleUrl: './document-list.component.css'
})
export class DocumentListComponent implements OnInit {
  searchQuery = '';
  documents: Document[] = [];
  isLoading = false;
  errorMsg = '';

  constructor(private documentService: DocumentService) {}

  ngOnInit(): void {
    this.loadDocuments();
  }

  loadDocuments(): void {
    this.isLoading = true;
    this.errorMsg = '';

    this.documentService.getAllDocuments().subscribe({
      next: (docs) => {
        this.documents = docs;
        this.isLoading = false;
      },
      error: () => {
        this.errorMsg = 'Fehler beim Laden der Dokumente.';
        this.isLoading = false;
      }
    });
  }

  onSearch(): void {
    if (!this.searchQuery.trim()) {
      this.loadDocuments();
      return;
    }

    this.isLoading = true;
    this.documentService.searchDocuments(this.searchQuery).subscribe({
      next: (results) => {
        this.documents = results;
        this.isLoading = false;
      },
      error: () => {
        this.errorMsg = 'Suche fehlgeschlagen.';
        this.isLoading = false;
      }
    });
  }

  shareDocument(doc: Document): void {
    this.documentService.createShareLink(doc.id).subscribe({
      next: (url) => {
        prompt('Hier ist dein Share-Link:', url);
      },
      error: () => alert('Konnte Link nicht erstellen.')
    });
  }
}
