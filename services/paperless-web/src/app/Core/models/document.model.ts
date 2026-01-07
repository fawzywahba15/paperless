export interface DocumentUploadRequest {
  file: File;
}

export interface DocumentResponse {
  id: string;
  filename: string;
  contentType: string;
  size: number;
  objectKey: string;
  status: string;
}

export enum DocumentStatus {
  PENDING = 'PENDING',
  PROCESSED = 'PROCESSED',
  FAILED = 'FAILED'
}
