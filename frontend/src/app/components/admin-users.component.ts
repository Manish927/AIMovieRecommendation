import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';

interface User {
  userID: number;
  name: string;
  email?: string;
  phone?: string;
}

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="px-4 py-6">
      <h2 class="text-2xl font-bold text-gray-900 mb-6">User Management</h2>

      <div *ngIf="error" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
        {{ error }}
      </div>

      <div *ngIf="loading" class="text-center py-8">
        <p class="text-gray-600">Loading users...</p>
      </div>

      <div *ngIf="!loading" class="bg-white rounded-lg shadow overflow-hidden">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Name</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Email</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Phone</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr *ngFor="let user of users">
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ user.userID }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ user.name }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ user.email || 'N/A' }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ user.phone || 'N/A' }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <button (click)="deleteUser(user.userID)" class="text-red-600 hover:text-red-900">Delete</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `
})
export class AdminUsersComponent implements OnInit {
  users: User[] = [];
  loading = false;
  error: string | null = null;
  private apiUrl = 'http://localhost:8081';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadUsers();
  }

  getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('adminToken');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  loadUsers() {
    this.loading = true;
    const headers = this.getAuthHeaders();
    this.http.get<User[]>(`${this.apiUrl}/admin/users`, { headers }).subscribe({
      next: (data) => {
        this.users = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load users';
        this.loading = false;
      }
    });
  }

  deleteUser(userId: number) {
    if (!confirm('Are you sure you want to delete this user?')) return;

    const headers = this.getAuthHeaders();
    this.http.delete(`${this.apiUrl}/admin/users/${userId}`, { headers }).subscribe({
      next: () => {
        this.loadUsers();
      },
      error: (err) => {
        this.error = 'Failed to delete user';
      }
    });
  }
}

