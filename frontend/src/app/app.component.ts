import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './components/header.component';
import { SignInModalComponent } from './components/sign-in-modal.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    HeaderComponent,
    SignInModalComponent
  ],
  template: `
    <div class="min-h-screen bg-gray-50">
      <!-- Header with Sign-In in top-right -->
      <app-header></app-header>
      
      <!-- Main Content with Router -->
      <main>
        <router-outlet></router-outlet>
      </main>

      <!-- Sign-In Modal -->
      <app-sign-in-modal></app-sign-in-modal>
    </div>
  `,
  styles: []
})
export class AppComponent {
}

