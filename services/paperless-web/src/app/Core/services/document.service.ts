import { Injectable } from '@angular/core';
import { HttpClient, HttpEvent, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../../shared/utils/api.config';
import { DocumentResponse } from '../models/ document.model';

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private readonly baseUrl = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.DOCUMENTS}`;

  constructor(private http: HttpClient) {}

  uploadDocument(file: File): Observable<DocumentResponse> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<DocumentResponse>(this.baseUrl, formData);
  }

  getDocument(id: string): Observable<DocumentResponse> {
    return this.http.get<DocumentResponse>(`${this.baseUrl}/${id}`);
  }
}
