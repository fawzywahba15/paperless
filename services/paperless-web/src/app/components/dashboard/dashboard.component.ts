import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { UploadFormComponent } from '../../shared/components/upload-form/upload-form.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [UploadFormComponent, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent {

  constructor(private router: Router) {}

  /**
   * Wird aufgerufen, wenn die Child-Component (UploadForm)
   * meldet, dass der Upload erfolgreich war.
   */
  onUploadFinished(): void {
    // Kurze Verzögerung für UX, dann Weiterleitung zur Liste
    setTimeout(() => {
      this.router.navigate(['/documents']);
    }, 1000);
  }
}
