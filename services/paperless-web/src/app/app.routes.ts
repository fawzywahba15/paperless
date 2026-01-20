import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { DocumentListComponent } from './components/document-list/document-list.component';
import { DocumentDetailComponent } from './components/document-detail/document-detail.component';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'documents', component: DocumentListComponent },
  { path: 'documents/:id', component: DocumentDetailComponent },

  // Wildcard Route für 404
  { path: '**', redirectTo: 'dashboard' }
];
