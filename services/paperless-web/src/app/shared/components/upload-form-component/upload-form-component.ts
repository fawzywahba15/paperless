import { Component, EventEmitter, Output } from '@angular/core';
import { DocumentService } from '../../../Core/services/document.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-upload-form',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './upload-form-component.html',
  styleUrls: ['./upload-form-component.css']
})
export class UploadFormComponent {
  selectedFile?: File;
  isLoading = false;
  errorMsg = '';
  successMsg = '';

  @Output() uploadSuccess = new EventEmitter<void>();

  constructor(private documentService: DocumentService) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.selectedFile = input.files[0];
      this.errorMsg = '';
      this.successMsg = '';
    }
  }

  uploadFile(): void {
    if (!this.selectedFile) return;

    this.isLoading = true;
    this.errorMsg = '';
    this.successMsg = '';

    this.documentService.uploadDocument(this.selectedFile).subscribe({
      next: (res) => {
        console.log('Upload success:', res);
        this.isLoading = false;
        this.successMsg = 'Upload erfolgreich! Verarbeitung läuft...';

        // Datei-Auswahl leeren
        this.selectedFile = undefined;

        // Parent benachrichtigen
        this.uploadSuccess.emit();
      },
      error: (err) => {
        console.error('Upload error:', err);
        this.isLoading = false;
        this.errorMsg = 'Upload fehlgeschlagen: ' + (err.message || 'Server Error');
      }
    });
  }
}
