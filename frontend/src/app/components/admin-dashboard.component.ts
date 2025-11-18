import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { AdminMoviesComponent } from './admin-movies.component';
import { AdminTheatersComponent } from './admin-theaters.component';
import { AdminUsersComponent } from './admin-users.component';
import { AdminBookingsComponent } from './admin-bookings.component';
import { AdminAnalyticsComponent } from './admin-analytics.component';

interface BookingStats {
  totalBookings: number;
  confirmedBookings: number;
  cancelledBookings: number;
  totalRevenue: number;
  averageBookingValue: number;
  totalSeatsBooked: number;
}

interface RevenueReport {
  totalRevenue: number;
  todayRevenue: number;
  thisWeekRevenue: number;
  thisMonthRevenue: number;
  revenueByTheater: { [key: string]: number };
  revenueByMovie: { [key: string]: number };
  totalBookings: number;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    AdminMoviesComponent,
    AdminTheatersComponent,
    AdminUsersComponent,
    AdminBookingsComponent,
    AdminAnalyticsComponent
  ],
  template: `
    <div class="min-h-screen bg-gray-100">
      <!-- Header -->
      <header class="bg-white shadow">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div class="flex justify-between items-center">
            <h1 class="text-2xl font-bold text-gray-900">Admin Dashboard</h1>
            <div class="flex items-center space-x-4">
              <span class="text-gray-700">Welcome, {{ adminUsername }}</span>
              <button
                (click)="logout()"
                class="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700">
                Logout
              </button>
            </div>
          </div>
        </div>
      </header>

      <!-- Navigation -->
      <nav class="bg-indigo-600 text-white">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div class="flex space-x-4">
            <button
              (click)="activeTab = 'dashboard'"
              [class.bg-indigo-700]="activeTab === 'dashboard'"
              class="px-4 py-2 rounded hover:bg-indigo-700 transition">
              Dashboard
            </button>
            <button
              (click)="activeTab = 'movies'"
              [class.bg-indigo-700]="activeTab === 'movies'"
              class="px-4 py-2 rounded hover:bg-indigo-700 transition">
              Movies
            </button>
            <button
              (click)="activeTab = 'theaters'"
              [class.bg-indigo-700]="activeTab === 'theaters'"
              class="px-4 py-2 rounded hover:bg-indigo-700 transition">
              Theaters
            </button>
            <button
              (click)="activeTab = 'users'"
              [class.bg-indigo-700]="activeTab === 'users'"
              class="px-4 py-2 rounded hover:bg-indigo-700 transition">
              Users
            </button>
            <button
              (click)="activeTab = 'bookings'"
              [class.bg-indigo-700]="activeTab === 'bookings'"
              class="px-4 py-2 rounded hover:bg-indigo-700 transition">
              Bookings
            </button>
            <button
              (click)="activeTab = 'analytics'"
              [class.bg-indigo-700]="activeTab === 'analytics'"
              class="px-4 py-2 rounded hover:bg-indigo-700 transition">
              Analytics
            </button>
          </div>
        </div>
      </nav>

      <!-- Content -->
      <main class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div *ngIf="activeTab === 'dashboard'">
          <div *ngIf="loading" class="text-center py-8">
            <p class="text-gray-600">Loading dashboard...</p>
          </div>

          <div *ngIf="!loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <!-- Stats Cards -->
            <div class="bg-white rounded-lg shadow p-6">
              <h3 class="text-sm font-medium text-gray-500">Total Bookings</h3>
              <p class="text-3xl font-bold text-gray-900 mt-2">{{ stats?.totalBookings || 0 }}</p>
            </div>
            <div class="bg-white rounded-lg shadow p-6">
              <h3 class="text-sm font-medium text-gray-500">Total Revenue</h3>
              <p class="text-3xl font-bold text-green-600 mt-2">&#36;{{ (stats?.totalRevenue || 0).toFixed(2) }}</p>
            </div>
            <div class="bg-white rounded-lg shadow p-6">
              <h3 class="text-sm font-medium text-gray-500">Confirmed Bookings</h3>
              <p class="text-3xl font-bold text-blue-600 mt-2">{{ stats?.confirmedBookings || 0 }}</p>
            </div>
            <div class="bg-white rounded-lg shadow p-6">
              <h3 class="text-sm font-medium text-gray-500">Avg Booking Value</h3>
              <p class="text-3xl font-bold text-purple-600 mt-2">&#36;{{ (stats?.averageBookingValue || 0).toFixed(2) }}</p>
            </div>
          </div>

          <!-- Revenue Summary -->
          <div *ngIf="revenueReport" class="bg-white rounded-lg shadow p-6 mb-8">
            <h2 class="text-xl font-bold text-gray-900 mb-4">Revenue Summary</h2>
            <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div>
                <p class="text-sm text-gray-500">Today</p>
                <p class="text-2xl font-bold text-green-600">&#36;{{ revenueReport.todayRevenue.toFixed(2) }}</p>
              </div>
              <div>
                <p class="text-sm text-gray-500">This Week</p>
                <p class="text-2xl font-bold text-blue-600">&#36;{{ revenueReport.thisWeekRevenue.toFixed(2) }}</p>
              </div>
              <div>
                <p class="text-sm text-gray-500">This Month</p>
                <p class="text-2xl font-bold text-purple-600">&#36;{{ revenueReport.thisMonthRevenue.toFixed(2) }}</p>
              </div>
              <div>
                <p class="text-sm text-gray-500">Total</p>
                <p class="text-2xl font-bold text-indigo-600">&#36;{{ revenueReport.totalRevenue.toFixed(2) }}</p>
              </div>
            </div>
          </div>
        </div>

        <div *ngIf="activeTab === 'movies'">
          <app-admin-movies></app-admin-movies>
        </div>

        <div *ngIf="activeTab === 'theaters'">
          <app-admin-theaters></app-admin-theaters>
        </div>

        <div *ngIf="activeTab === 'users'">
          <app-admin-users></app-admin-users>
        </div>

        <div *ngIf="activeTab === 'bookings'">
          <app-admin-bookings></app-admin-bookings>
        </div>

        <div *ngIf="activeTab === 'analytics'">
          <app-admin-analytics></app-admin-analytics>
        </div>
      </main>
    </div>
  `
})
export class AdminDashboardComponent implements OnInit {
  activeTab = 'dashboard';
  loading = false;
  stats: BookingStats | null = null;
  revenueReport: RevenueReport | null = null;
  adminUsername = '';
  private apiUrl = 'http://localhost:8081';

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() {
    const admin = localStorage.getItem('admin');
    if (!admin) {
      this.router.navigate(['/admin/login']);
      return;
    }
    this.adminUsername = JSON.parse(admin).username;
    this.loadDashboard();
  }

  getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('adminToken');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  loadDashboard() {
    this.loading = true;
    const headers = this.getAuthHeaders();

    this.http.get<BookingStats>(`${this.apiUrl}/admin/bookings/stats`, { headers }).subscribe({
      next: (data) => {
        this.stats = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load stats', err);
        this.loading = false;
      }
    });

    this.http.get<RevenueReport>(`${this.apiUrl}/admin/analytics/revenue`, { headers }).subscribe({
      next: (data) => {
        this.revenueReport = data;
      },
      error: (err) => {
        console.error('Failed to load revenue report', err);
      }
    });
  }

  logout() {
    localStorage.removeItem('adminToken');
    localStorage.removeItem('admin');
    window.location.hash = 'admin-login';
    window.location.reload();
  }
}

