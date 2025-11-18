import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';

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
  selector: 'app-admin-analytics',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="px-4 py-6">
      <h2 class="text-2xl font-bold text-gray-900 mb-6">Analytics & Reports</h2>

      <div *ngIf="loading" class="text-center py-8">
        <p class="text-gray-600">Loading analytics...</p>
      </div>

      <div *ngIf="!loading">
        <!-- Booking Statistics -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <div class="bg-white rounded-lg shadow p-6">
            <h3 class="text-lg font-semibold text-gray-700 mb-2">Total Bookings</h3>
            <p class="text-3xl font-bold text-indigo-600">{{ stats?.totalBookings || 0 }}</p>
          </div>
          <div class="bg-white rounded-lg shadow p-6">
            <h3 class="text-lg font-semibold text-gray-700 mb-2">Confirmed</h3>
            <p class="text-3xl font-bold text-green-600">{{ stats?.confirmedBookings || 0 }}</p>
          </div>
          <div class="bg-white rounded-lg shadow p-6">
            <h3 class="text-lg font-semibold text-gray-700 mb-2">Cancelled</h3>
            <p class="text-3xl font-bold text-red-600">{{ stats?.cancelledBookings || 0 }}</p>
          </div>
        </div>

        <!-- Revenue Statistics -->
        <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div class="bg-white rounded-lg shadow p-6">
            <h3 class="text-sm font-medium text-gray-500 mb-1">Total Revenue</h3>
            <p class="text-2xl font-bold text-green-600">&#36;{{ (revenueReport?.totalRevenue || 0).toFixed(2) }}</p>
          </div>
          <div class="bg-white rounded-lg shadow p-6">
            <h3 class="text-sm font-medium text-gray-500 mb-1">Today</h3>
            <p class="text-2xl font-bold text-blue-600">&#36;{{ (revenueReport?.todayRevenue || 0).toFixed(2) }}</p>
          </div>
          <div class="bg-white rounded-lg shadow p-6">
            <h3 class="text-sm font-medium text-gray-500 mb-1">This Week</h3>
            <p class="text-2xl font-bold text-purple-600">&#36;{{ (revenueReport?.thisWeekRevenue || 0).toFixed(2) }}</p>
          </div>
          <div class="bg-white rounded-lg shadow p-6">
            <h3 class="text-sm font-medium text-gray-500 mb-1">This Month</h3>
            <p class="text-2xl font-bold text-indigo-600">&#36;{{ (revenueReport?.thisMonthRevenue || 0).toFixed(2) }}</p>
          </div>
        </div>

        <!-- Additional Stats -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div class="bg-white rounded-lg shadow p-6">
            <h3 class="text-lg font-semibold text-gray-700 mb-4">Booking Metrics</h3>
            <div class="space-y-2">
              <div class="flex justify-between">
                <span class="text-gray-600">Average Booking Value:</span>
                <span class="font-semibold">&#36;{{ (stats?.averageBookingValue || 0).toFixed(2) }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">Total Seats Booked:</span>
                <span class="font-semibold">{{ stats?.totalSeatsBooked || 0 }}</span>
              </div>
            </div>
          </div>
          <div class="bg-white rounded-lg shadow p-6">
            <h3 class="text-lg font-semibold text-gray-700 mb-4">Revenue Summary</h3>
            <div class="space-y-2">
              <div class="flex justify-between">
                <span class="text-gray-600">Total Bookings:</span>
                <span class="font-semibold">{{ revenueReport?.totalBookings || 0 }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">Total Revenue:</span>
                <span class="font-semibold text-green-600">&#36;{{ (revenueReport?.totalRevenue || 0).toFixed(2) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class AdminAnalyticsComponent implements OnInit {
  stats: BookingStats | null = null;
  revenueReport: RevenueReport | null = null;
  loading = false;
  private apiUrl = 'http://localhost:8081';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadAnalytics();
  }

  getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('adminToken');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  loadAnalytics() {
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
}

