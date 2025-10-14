import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { API_CONFIG } from '../../shared/utils/api.config';
// @ts-ignore
import { DocumentResponse } from '../models/document.model';

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private readonly baseUrl = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.DOCUMENTS}`;

  constructor(private http: HttpClient) {}

  uploadDocument(file: File): Observable<DocumentResponse> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<DocumentResponse>(this.baseUrl, formData, {
      observe: 'body',
      responseType: 'json'
    }).pipe(
      catchError((error: HttpErrorResponse) => {
        console.error(' [DocumentService] Upload-Fehler:', error);
        if (error.status === 0) {
          console.error(' Backend nicht erreichbar oder CORS-Problem.');
        } else if (error.status === 400) {
          console.error('️ Ungültige Anfrage – evtl. fehlender Parameter.');
        } else if (error.status >= 500) {
          console.error(' Serverfehler beim Upload.');
        }
        return throwError(() => new Error('Upload fehlgeschlagen.'));
      })
    );
  }

  getDocument(id: string): Observable<DocumentResponse> {
    return this.http.get<DocumentResponse>(`${this.baseUrl}/${id}`);
  }
}
