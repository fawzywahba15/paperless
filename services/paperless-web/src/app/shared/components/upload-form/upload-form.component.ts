import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DocumentService } from '../../../core/services/document.service';

@Component({
  selector: 'app-upload-form',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './upload-form.component.html',
  styleUrl: './upload-form.component.css'
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
      const file = input.files[0];

      if (file.type !== 'application/pdf') {
        this.errorMsg = 'Nur PDF-Dateien sind erlaubt!';
        this.selectedFile = undefined;
        input.value = ''; // Reset Input
        return;
      }

      this.selectedFile = file;
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
      next: () => {
        this.isLoading = false;
        this.successMsg = 'Upload erfolgreich!';
        this.selectedFile = undefined;

        const fileInput = document.getElementById('fileInput') as HTMLInputElement;
        if(fileInput) fileInput.value = '';

        this.uploadSuccess.emit();
      },
      error: (err: any) => {
        console.error(err);
        this.isLoading = false;
        this.errorMsg = 'Upload fehlgeschlagen.';
      }
    });
  }
}
