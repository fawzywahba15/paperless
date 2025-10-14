import { Routes } from '@angular/router';
import {UploadFormComponent} from './shared/components/upload-form-component/upload-form-component';

export const routes: Routes = [
  { path: 'upload', component: UploadFormComponent },
  { path: '', redirectTo: '/upload', pathMatch: 'full' } // optional: redirect auf /upload
];
