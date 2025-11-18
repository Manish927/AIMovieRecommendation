import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';

interface AdminLoginRequest {
  username: string;
  password: string;
}

interface AdminLoginResponse {
  token: string;
  admin: {
    adminId: number;
    username: string;
    email: string;
    role: string;
  };
  message: string;
}

@Component({
  selector: 'app-admin-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gray-100 py-12 px-4 sm:px-6 lg:px-8">
      <div class="max-w-md w-full space-y-8 bg-white p-8 rounded-lg shadow-lg">
        <div>
          <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
            Admin Login
          </h2>
          <p class="mt-2 text-center text-sm text-gray-600">
            Sign in to access the administrative panel
          </p>
        </div>
        <form class="mt-8 space-y-6" (ngSubmit)="login()">
          <div *ngIf="error" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            {{ error }}
          </div>
          <div class="rounded-md shadow-sm -space-y-px">
            <div>
              <label for="username" class="sr-only">Username</label>
              <input
                id="username"
                name="username"
                type="text"
                required
                [(ngModel)]="loginData.username"
                class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
                placeholder="Username">
            </div>
            <div>
              <label for="password" class="sr-only">Password</label>
              <input
                id="password"
                name="password"
                type="password"
                required
                [(ngModel)]="loginData.password"
                class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
                placeholder="Password">
            </div>
          </div>

          <div>
            <button
              type="submit"
              [disabled]="loading"
              class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50">
              {{ loading ? 'Signing in...' : 'Sign in' }}
            </button>
          </div>
          <div class="text-sm text-gray-600 text-center">
            <p>Default credentials:</p>
            <p>Username: <strong>admin</strong> | Password: <strong>admin123</strong></p>
          </div>
        </form>
      </div>
    </div>
  `
})
export class AdminLoginComponent {
  loginData: AdminLoginRequest = { username: '', password: '' };
  loading = false;
  error: string | null = null;
  private apiUrl = 'http://localhost:8081';

  constructor(private http: HttpClient) {}

  login() {
    this.loading = true;
    this.error = null;

    this.http.post<AdminLoginResponse>(`${this.apiUrl}/admin/login`, this.loginData).subscribe({
      next: (response) => {
        localStorage.setItem('adminToken', response.token);
        localStorage.setItem('admin', JSON.stringify(response.admin));
        this.loading = false;
        window.location.hash = 'admin-dashboard';
        window.location.reload();
      },
      error: (err) => {
        this.error = err.error?.message || 'Invalid username or password';
        this.loading = false;
      }
    });
  }
}

