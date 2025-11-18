import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';

interface Theater {
  theaterId?: number;
  name: string;
  address?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  phone?: string;
  totalScreens?: number;
}

@Component({
  selector: 'app-admin-theaters',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="px-4 py-6">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-2xl font-bold text-gray-900">Theater Management</h2>
        <button
          (click)="showAddForm = !showAddForm"
          class="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700">
          {{ showAddForm ? 'Cancel' : 'Add New Theater' }}
        </button>
      </div>

      <div *ngIf="error" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
        {{ error }}
      </div>

      <!-- Add/Edit Form -->
      <div *ngIf="showAddForm || editingTheater" class="bg-white rounded-lg shadow p-6 mb-6">
        <h3 class="text-lg font-semibold mb-4">{{ editingTheater ? 'Edit Theater' : 'Add New Theater' }}</h3>
        <form (ngSubmit)="saveTheater()">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">Name *</label>
              <input type="text" [(ngModel)]="currentTheater.name" name="name" required
                class="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2">
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">City</label>
              <input type="text" [(ngModel)]="currentTheater.city" name="city"
                class="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2">
            </div>
            <div class="md:col-span-2">
              <label class="block text-sm font-medium text-gray-700">Address</label>
              <input type="text" [(ngModel)]="currentTheater.address" name="address"
                class="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2">
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">State</label>
              <input type="text" [(ngModel)]="currentTheater.state" name="state"
                class="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2">
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">Zip Code</label>
              <input type="text" [(ngModel)]="currentTheater.zipCode" name="zipCode"
                class="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2">
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">Phone</label>
              <input type="text" [(ngModel)]="currentTheater.phone" name="phone"
                class="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2">
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">Total Screens</label>
              <input type="number" [(ngModel)]="currentTheater.totalScreens" name="totalScreens"
                class="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2">
            </div>
          </div>
          <div class="mt-4 flex justify-end space-x-2">
            <button type="button" (click)="cancelEdit()" class="px-4 py-2 border border-gray-300 rounded hover:bg-gray-50">
              Cancel
            </button>
            <button type="submit" class="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700">
              {{ editingTheater ? 'Update' : 'Add' }} Theater
            </button>
          </div>
        </form>
      </div>

      <!-- Theaters List -->
      <div class="bg-white rounded-lg shadow overflow-hidden">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Name</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">City</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Screens</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr *ngFor="let theater of theaters">
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ theater.theaterId }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ theater.name }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ theater.city }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ theater.totalScreens || 0 }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                <button (click)="editTheater(theater)" class="text-indigo-600 hover:text-indigo-900">Edit</button>
                <button (click)="deleteTheater(theater.theaterId!)" class="text-red-600 hover:text-red-900">Delete</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `
})
export class AdminTheatersComponent implements OnInit {
  theaters: Theater[] = [];
  currentTheater: Theater = { name: '' };
  showAddForm = false;
  editingTheater: Theater | null = null;
  loading = false;
  error: string | null = null;
  private apiUrl = 'http://localhost:8081';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadTheaters();
  }

  getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('adminToken');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  loadTheaters() {
    this.loading = true;
    this.http.get<Theater[]>(`${this.apiUrl}/theaters`).subscribe({
      next: (data) => {
        this.theaters = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load theaters';
        this.loading = false;
      }
    });
  }

  saveTheater() {
    const headers = this.getAuthHeaders();
    const theaterData = { ...this.currentTheater };

    if (this.editingTheater) {
      this.http.put<Theater>(`${this.apiUrl}/admin/theaters/${this.editingTheater.theaterId}`, theaterData, { headers }).subscribe({
        next: () => {
          this.loadTheaters();
          this.cancelEdit();
        },
        error: (err) => {
          this.error = 'Failed to update theater';
        }
      });
    } else {
      this.http.post<Theater>(`${this.apiUrl}/admin/theaters`, theaterData, { headers }).subscribe({
        next: () => {
          this.loadTheaters();
          this.cancelEdit();
        },
        error: (err) => {
          this.error = 'Failed to create theater';
        }
      });
    }
  }

  editTheater(theater: Theater) {
    this.editingTheater = theater;
    this.currentTheater = { ...theater };
    this.showAddForm = true;
  }

  deleteTheater(theaterId: number) {
    if (!confirm('Are you sure you want to delete this theater?')) return;

    const headers = this.getAuthHeaders();
    this.http.delete(`${this.apiUrl}/admin/theaters/${theaterId}`, { headers }).subscribe({
      next: () => {
        this.loadTheaters();
      },
      error: (err) => {
        this.error = 'Failed to delete theater';
      }
    });
  }

  cancelEdit() {
    this.showAddForm = false;
    this.editingTheater = null;
    this.currentTheater = { name: '' };
  }
}

