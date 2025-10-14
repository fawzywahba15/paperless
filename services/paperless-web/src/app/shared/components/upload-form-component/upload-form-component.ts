import { Component } from '@angular/core';
import {DocumentService} from '../../../Core/services/document.service';
import {DocumentResponse} from '../../../Core/models/ document.model';
import {JsonPipe, NgIf} from '@angular/common';

@Component({
  selector: 'app-upload-form',
  templateUrl: './upload-form-component.html',
  standalone: true,
  imports: [
    NgIf,
    JsonPipe
  ],
  styleUrls: ['./upload-form-component.css']
})
export class UploadFormComponent {
  selectedFile?: File;
  uploadedDoc?: DocumentResponse;
  isLoading = false;
  errorMsg = '';

  constructor(private documentService: DocumentService) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.selectedFile = input.files[0];
    }
  }

  uploadFile(): void {
    if (!this.selectedFile) return;
    this.isLoading = true;
    this.errorMsg = '';

    this.documentService.uploadDocument(this.selectedFile).subscribe({
      next: (res) => {
        this.uploadedDoc = res;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMsg = 'Upload fehlgeschlagen. Bitte erneut versuchen.';
        console.error(err);
        this.isLoading = false;
      }
    });
  }
}
