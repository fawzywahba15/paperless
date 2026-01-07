import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { API_CONFIG } from '../../shared/utils/api.config';

// Interface für Suchergebnisse
export interface Document {
  id: string;
  title: string;
  content?: string;
  filename?: string;
}

@Injectable({ providedIn: 'root' })
export class DocumentService {
  // Basis URL für Dokumente
  private readonly baseUrl = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.DOCUMENTS}`;
  // Basis URL für Sharing (manuell bauen, falls sie nicht in der config ist)
  private readonly shareUrl = `${API_CONFIG.BASE_URL}/share`;

  constructor(private http: HttpClient) {}

  uploadDocument(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post(this.baseUrl, formData, {
      observe: 'body',
      responseType: 'json'
    }).pipe(
      catchError(this.handleError)
    );
  }

  // Suche
  searchDocuments(query: string): Observable<Document[]> {
    return this.http.get<Document[]>(`${this.baseUrl}/search?query=${query}`);
  }

  // Sharing
  shareDocument(id: string): Observable<string> {
    // responseType: 'text', weil das Backend einen String zurückgibt
    return this.http.post(`${this.shareUrl}/${id}`, {}, { responseType: 'text' });
  }

  private handleError(error: HttpErrorResponse) {
    console.error(' [DocumentService] Fehler:', error);
    return throwError(() => new Error('Aktion fehlgeschlagen.'));
  }
}
