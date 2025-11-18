import { Component, EventEmitter, Output, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-sign-in-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div 
      *ngIf="isOpen"
      class="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4"
      (click)="closeModal($event)">
      <div 
        class="bg-white rounded-lg shadow-xl max-w-md w-full p-6"
        (click)="$event.stopPropagation()">
        <!-- Close Button -->
        <div class="flex justify-between items-center mb-6">
          <h2 class="text-2xl font-bold text-gray-900">Sign In</h2>
          <button 
            (click)="close()"
            class="text-gray-400 hover:text-gray-600 transition">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>

        <!-- Error Message -->
        <div *ngIf="errorMessage" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {{ errorMessage }}
        </div>

        <!-- Success Message -->
        <div *ngIf="successMessage" class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
          {{ successMessage }}
        </div>

        <!-- Sign In Form -->
        <form (ngSubmit)="onSubmit()" *ngIf="!showSignUp">
          <div class="mb-4">
            <label class="block text-gray-700 text-sm font-medium mb-2">Email or Phone</label>
            <input 
              type="text"
              [(ngModel)]="loginData.emailOrPhone"
              name="emailOrPhone"
              required
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent outline-none">
          </div>

          <div class="mb-6">
            <label class="block text-gray-700 text-sm font-medium mb-2">Password</label>
            <input 
              type="password"
              [(ngModel)]="loginData.password"
              name="password"
              required
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent outline-none">
          </div>

          <button 
            type="submit"
            [disabled]="loading"
            class="w-full bg-red-600 text-white py-3 rounded-lg font-medium hover:bg-red-700 transition disabled:opacity-50 disabled:cursor-not-allowed">
            {{ loading ? 'Signing In...' : 'Sign In' }}
          </button>
        </form>

        <!-- Sign Up Form -->
        <form (ngSubmit)="onSignUp()" *ngIf="showSignUp">
          <div class="mb-4">
            <label class="block text-gray-700 text-sm font-medium mb-2">Name</label>
            <input 
              type="text"
              [(ngModel)]="signUpData.name"
              name="name"
              required
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent outline-none">
          </div>

          <div class="mb-4">
            <label class="block text-gray-700 text-sm font-medium mb-2">Email</label>
            <input 
              type="email"
              [(ngModel)]="signUpData.email"
              name="email"
              required
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent outline-none">
          </div>

          <div class="mb-4">
            <label class="block text-gray-700 text-sm font-medium mb-2">Phone</label>
            <input 
              type="tel"
              [(ngModel)]="signUpData.phone"
              name="phone"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent outline-none">
          </div>

          <div class="mb-6">
            <label class="block text-gray-700 text-sm font-medium mb-2">Password</label>
            <input 
              type="password"
              [(ngModel)]="signUpData.password"
              name="password"
              required
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent outline-none">
          </div>

          <button 
            type="submit"
            [disabled]="loading"
            class="w-full bg-red-600 text-white py-3 rounded-lg font-medium hover:bg-red-700 transition disabled:opacity-50 disabled:cursor-not-allowed">
            {{ loading ? 'Signing Up...' : 'Sign Up' }}
          </button>
        </form>

        <!-- Toggle between Sign In and Sign Up -->
        <div class="mt-4 text-center">
          <p class="text-gray-600 text-sm">
            <span *ngIf="!showSignUp">New to CineBook Pro? </span>
            <span *ngIf="showSignUp">Already have an account? </span>
            <button 
              (click)="toggleForm()"
              class="text-red-600 font-medium hover:underline">
              {{ showSignUp ? 'Sign In' : 'Sign Up' }}
            </button>
          </p>
        </div>
      </div>
    </div>
  `
})
export class SignInModalComponent implements OnInit, OnDestroy {
  @Output() closeModalEvent = new EventEmitter<void>();
  @Output() loginSuccess = new EventEmitter<void>();

  isOpen = false;
  showSignUp = false;
  loading = false;
  errorMessage = '';
  successMessage = '';

  loginData = {
    emailOrPhone: '',
    password: ''
  };

  signUpData = {
    name: '',
    email: '',
    phone: '',
    password: ''
  };

  private apiUrl = 'http://localhost:8081';
  private openSignInHandler?: () => void;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    // Listen for open modal event
    this.openSignInHandler = () => {
      this.open();
    };
    window.addEventListener('openSignIn', this.openSignInHandler);
  }

  ngOnDestroy() {
    // Clean up event listener
    if (this.openSignInHandler) {
      window.removeEventListener('openSignIn', this.openSignInHandler);
    }
  }

  open() {
    this.isOpen = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.showSignUp = false;
  }

  close() {
    this.isOpen = false;
    this.closeModalEvent.emit();
  }

  closeModal(event: MouseEvent) {
    if (event.target === event.currentTarget) {
      this.close();
    }
  }

  toggleForm() {
    this.showSignUp = !this.showSignUp;
    this.errorMessage = '';
    this.successMessage = '';
  }

  onSubmit() {
    this.loading = true;
    this.errorMessage = '';
    
    const loginPayload = {
      email: this.loginData.emailOrPhone,
      password: this.loginData.password
    };

    this.http.post<any>(`${this.apiUrl}/auth/login`, loginPayload).subscribe({
      next: (response) => {
        if (response.token) {
          localStorage.setItem('authToken', response.token);
          if (response.user) {
            localStorage.setItem('user', JSON.stringify(response.user));
          }
          this.successMessage = 'Sign in successful!';
          setTimeout(() => {
            this.close();
            this.loginSuccess.emit();
            window.location.reload();
          }, 1000);
        }
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Invalid email or password. Please try again.';
        this.loading = false;
        console.error(err);
      }
    });
  }

  onSignUp() {
    this.loading = true;
    this.errorMessage = '';

    const signUpPayload = {
      name: this.signUpData.name,
      email: this.signUpData.email,
      phone: this.signUpData.phone || '',
      password: this.signUpData.password
    };

    this.http.post<any>(`${this.apiUrl}/auth/register`, signUpPayload).subscribe({
      next: (response) => {
        this.successMessage = 'Registration successful! Please sign in.';
        setTimeout(() => {
          this.showSignUp = false;
          this.loading = false;
        }, 2000);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Registration failed. Please try again.';
        this.loading = false;
        console.error(err);
      }
    });
  }
}

