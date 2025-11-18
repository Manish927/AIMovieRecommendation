import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-user-registration',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="max-w-2xl mx-auto px-4 py-6">
      <h2 class="text-3xl font-bold text-gray-900 mb-6">User Registration</h2>
      
      <div *ngIf="successMessage" class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
        {{ successMessage }}
      </div>
      
      <div *ngIf="errorMessage" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
        {{ errorMessage }}
      </div>

      <form (ngSubmit)="onSubmit()" class="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4">
        <div class="mb-4">
          <label class="block text-gray-700 text-sm font-bold mb-2" for="userId">
            User ID *
          </label>
          <input 
            [(ngModel)]="user.userId"
            name="userId"
            id="userId"
            type="number" 
            required
            class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline">
        </div>

        <div class="mb-4">
          <label class="block text-gray-700 text-sm font-bold mb-2" for="name">
            Name *
          </label>
          <input 
            [(ngModel)]="user.name"
            name="name"
            id="name"
            type="text" 
            required
            class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline">
        </div>

        <div class="mb-4">
          <label class="block text-gray-700 text-sm font-bold mb-2" for="email">
            Email *
          </label>
          <input 
            [(ngModel)]="user.email"
            name="email"
            id="email"
            type="email" 
            required
            class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline">
        </div>

        <div class="mb-4">
          <label class="block text-gray-700 text-sm font-bold mb-2" for="phone">
            Phone
          </label>
          <input 
            [(ngModel)]="user.phone"
            name="phone"
            id="phone"
            type="tel"
            class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline">
        </div>

        <div class="mb-4">
          <label class="block text-gray-700 text-sm font-bold mb-2" for="password">
            Password *
          </label>
          <input 
            [(ngModel)]="user.password"
            name="password"
            id="password"
            type="password" 
            required
            class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline">
        </div>

        <div class="flex items-center justify-between">
          <button 
            type="submit"
            [disabled]="submitting"
            class="bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline disabled:opacity-50">
            {{ submitting ? 'Registering...' : 'Register' }}
          </button>
          <button 
            type="button"
            (click)="resetForm()"
            class="bg-gray-500 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline">
            Reset
          </button>
        </div>
      </form>
    </div>
  `
})
export class UserRegistrationComponent {
  user = {
    userId: null as number | null,
    name: '',
    email: '',
    phone: '',
    password: ''
  };
  
  submitting = false;
  successMessage = '';
  errorMessage = '';
  private apiUrl = 'http://localhost:8081';

  constructor(private http: HttpClient) {}

  onSubmit() {
    if (!this.user.userId || !this.user.name || !this.user.email || !this.user.password) {
      this.errorMessage = 'Please fill in all required fields.';
      return;
    }

    this.submitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    const userData = {
      userID: this.user.userId,
      name: this.user.name,
      email: this.user.email,
      phone: this.user.phone || '',
      password: this.user.password
    };

    this.http.post(`${this.apiUrl}/auth/register`, userData).subscribe({
      next: (response) => {
        this.successMessage = `User registered successfully!`;
        this.submitting = false;
        this.resetForm();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Registration failed. Please try again.';
        this.submitting = false;
        console.error(err);
      }
    });
  }

  resetForm() {
    this.user = {
      userId: null,
      name: '',
      email: '',
      phone: '',
      password: ''
    };
    this.errorMessage = '';
    this.successMessage = '';
  }
}


