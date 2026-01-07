import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { DocumentService, Document } from './Core/services/document.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UploadFormComponent } from './shared/components/upload-form-component/upload-form-component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, FormsModule, UploadFormComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  title = 'Paperless';
  searchQuery: string = '';
  documents: Document[] = [];
  isLoading = false;

  constructor(private documentService: DocumentService) {}

  // 1. search ausführen
  onSearch() {
    // Falls leer, breche ab
    if (!this.searchQuery.trim()) return;

    this.isLoading = true;
    console.log('Searching for:', this.searchQuery);

    this.documentService.searchDocuments(this.searchQuery).subscribe({
      next: (docs) => {
        this.documents = docs;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Search failed', err);
        this.isLoading = false;
      }
    });
  }

  // 2. Share Link generieren
  onShare(doc: Document) {
    this.documentService.shareDocument(doc.id).subscribe({
      next: (link) => {
        window.prompt('Shareable Link (valid for 7 days):', link);
      },
      error: (err) => alert('Sharing failed! check console.')
    });
  }
}
