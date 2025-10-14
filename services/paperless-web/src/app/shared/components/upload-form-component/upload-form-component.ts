import { Component } from '@angular/core';
import { DocumentService } from '../../../Core/services/document.service';
// @ts-ignore
import { DocumentResponse } from '../../../Core/models/document.model';
import { JsonPipe, NgIf } from '@angular/common';

@Component({
  selector: 'app-upload-form',
  templateUrl: './upload-form-component.html',
  standalone: true,
  imports: [NgIf, JsonPipe],
  styleUrls: ['./upload-form-component.css']
})
export class UploadFormComponent {
  selectedFile?: File;
  uploadedDoc?: DocumentResponse;
  isLoading = false;
  errorMsg = '';
  successMsg = '';

  constructor(private documentService: DocumentService) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.selectedFile = input.files[0];
      console.log(' Datei ausgewählt:', this.selectedFile.name);
    }
  }

  uploadFile(): void {
    if (!this.selectedFile) {
      this.errorMsg = '⚠ Bitte zuerst eine Datei auswählen.';
      return;
    }

    this.isLoading = true;
    this.errorMsg = '';
    this.successMsg = '';
    console.log(' Upload gestartet...');

    this.documentService.uploadDocument(this.selectedFile).subscribe({
      next: (res) => {
        console.log(' Upload erfolgreich:', res);
        this.uploadedDoc = res;
        this.successMsg = `Datei "${res.filename}" wurde erfolgreich hochgeladen!`;
        this.isLoading = false;
      },
      error: (err) => {
        console.error(' Upload fehlgeschlagen:', err);
        this.errorMsg = 'Upload fehlgeschlagen. Bitte Verbindung oder Server prüfen.';
        this.isLoading = false;
      }
    });
  }
}
