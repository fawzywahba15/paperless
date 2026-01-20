export interface Document {
  id: string;
  title: string;
  category: string;
  summary?: string;
  filename: string;
  contentType: string;
  size: number;
  status: DocumentStatus;
  content?: string; // Das ist der OCR-Text (im Backend 'content' gemappt)
}

export enum DocumentStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}
