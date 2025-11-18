import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';

interface BookingReport {
  bookingId: number;
  userId: number;
  userName: string;
  movieId: number;
  movieTitle: string;
  theaterId: number;
  theaterName: string;
  numberOfSeats: number;
  totalPrice: number;
  pricePerTicket: number;
  bookingTime: string;
  status: string;
}

@Component({
  selector: 'app-admin-bookings',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="px-4 py-6">
      <h2 class="text-2xl font-bold text-gray-900 mb-6">Booking Reports</h2>

      <div *ngIf="error" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
        {{ error }}
      </div>

      <div *ngIf="loading" class="text-center py-8">
        <p class="text-gray-600">Loading bookings...</p>
      </div>

      <div *ngIf="!loading" class="bg-white rounded-lg shadow overflow-hidden">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Booking ID</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">User</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Movie</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Theater</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Seats</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Total Price</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Booking Time</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr *ngFor="let booking of bookings">
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ booking.bookingId }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ booking.userName }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ booking.movieTitle }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ booking.theaterName }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ booking.numberOfSeats }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 font-semibold">&#36;{{ booking.totalPrice.toFixed(2) }}</td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span [class]="getStatusClass(booking.status)" class="px-2 py-1 rounded text-xs font-medium">
                  {{ booking.status }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ formatDate(booking.bookingTime) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `
})
export class AdminBookingsComponent implements OnInit {
  bookings: BookingReport[] = [];
  loading = false;
  error: string | null = null;
  private apiUrl = 'http://localhost:8081';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadBookings();
  }

  getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('adminToken');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  loadBookings() {
    this.loading = true;
    const headers = this.getAuthHeaders();
    this.http.get<BookingReport[]>(`${this.apiUrl}/admin/bookings`, { headers }).subscribe({
      next: (data) => {
        this.bookings = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load bookings';
        this.loading = false;
      }
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'CONFIRMED':
        return 'bg-green-100 text-green-800';
      case 'CANCELLED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  formatDate(dateString: string): string {
    if (!dateString) return '';
    return new Date(dateString).toLocaleString();
  }
}

