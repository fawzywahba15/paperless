import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../../shared/utils/api.config';
import { Document } from '../models/document.model';

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private readonly baseUrl = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.DOCUMENTS}`;
  private readonly shareUrl = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.SHARE}`;

  constructor(private http: HttpClient) {}

  /**
   * Lädt ein Dokument hoch (Multipart File).
   */
  uploadDocument(file: File, title?: string): Observable<void> {
    const formData = new FormData();
    formData.append('file', file);
    if (title) {
      formData.append('title', title);
    }

    return this.http.post<void>(this.baseUrl, formData);
  }

  /**
   * Holt alle Dokumente aus der DB.
   */
  getAllDocuments(): Observable<Document[]> {
    return this.http.get<Document[]>(this.baseUrl);
  }

  /**
   * Holt ein einzelnes Dokument inkl. OCR-Text.
   */
  getDocumentById(id: string): Observable<Document> {
    return this.http.get<Document>(`${this.baseUrl}/${id}`);
  }

  /**
   * Sucht Dokumente via ElasticSearch (Query Param).
   */
  searchDocuments(query: string): Observable<any[]> { // ElasticDocument hat etwas andere Felder
    const params = new HttpParams().set('query', query);
    return this.http.get<any[]>(`${this.baseUrl}/search`, { params });
  }

  /**
   * Erstellt einen Share-Link.
   * Backend gibt den Link als String zurück (text/plain).
   */
  createShareLink(id: string): Observable<string> {
    return this.http.post(`${this.shareUrl}/${id}`, {}, { responseType: 'text' });
  }
}
